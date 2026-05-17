package GameLogic;

import Defense.Defense;
import Defense.DefenseAttacker;
import Defense.DefenseHealer;
import Defense.DefenseMultipleAttack;
import Defense.DefenseType;
import Entity.Entity;
import Entity.EntityAttacker;
import Entity.EntityHealer;
import Table.GameBoard;
import Zombie.Zombie;
import Zombie.ZombieAttacker;
import Zombie.ZombieHealer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CombatManager {

    private final Lock combatLock = new ReentrantLock();

    private final GameState state;
    private final GameBoard board;
    private final ArrayList<Zombie> waveZombies;
    private final ArrayList<Defense> waveDefense;
    private CombatLog combatLog;
    private Defense lifeTree;
    private Runnable onEntityChanged;
    private Runnable onVictoryCheck;
    private Runnable onLossCheck;

    public CombatManager(GameState state, GameBoard board,
                         ArrayList<Zombie> waveZombies, ArrayList<Defense> waveDefense) {
        this.state = state;
        this.board = board;
        this.waveZombies = waveZombies;
        this.waveDefense = waveDefense;
    }

    public void setCombatLog(CombatLog log) { this.combatLog = log; }
    public void setLifeTree(Defense tree) { this.lifeTree = tree; }

    public void setOnEntityChanged(Runnable r) { this.onEntityChanged = r; }
    public void setOnVictoryCheck(Runnable r) { this.onVictoryCheck = r; }
    public void setOnLossCheck(Runnable r) { this.onLossCheck = r; }

    public void processCombat() {
        if (state.isPaused() || !state.isRoundActive()) return;

        ArrayList<Defense> defensesCopy = new ArrayList<>(waveDefense);
        for (Defense d : defensesCopy) {
            if (d == null || d.getHealthPoints() <= 0) continue;
            if (d.isHealer()) processHealing(d);
            else processDefenseAttack(d);
        }

        ArrayList<Zombie> zombiesCopy = new ArrayList<>(waveZombies);
        for (Zombie z : zombiesCopy) {
            if (z == null || !z.isAlive() || z.getHealthPoints() <= 0) continue;
            if (z.isHealer()) processHealing(z);
            else processZombieAttack(z);
        }

        removeDeadEntities();

        if (onEntityChanged != null) onEntityChanged.run();
        if (onVictoryCheck != null) onVictoryCheck.run();
        if (onLossCheck != null) onLossCheck.run();
    }

    public void processDefenseAttackThreaded(Defense defense) {
        combatLock.lock();
        try {
            if (defense == null || defense.getHealthPoints() <= 0) return;
            if (defense.isHealer()) processHealing(defense);
            else processDefenseAttack(defense);
        } finally {
            combatLock.unlock();
        }
    }

    public void processZombieAttackThreaded(Zombie zombie) {
        combatLock.lock();
        try {
            if (zombie == null || !zombie.isAlive() || zombie.getHealthPoints() <= 0) return;
            if (zombie.isHealer()) processHealing(zombie);
            else processZombieAttack(zombie);
        } finally {
            combatLock.unlock();
        }
    }

    private void processDefenseAttack(Defense defense) {
        if (defense.hasType(DefenseType.BLOCKS)) return;

        ArrayList<Entity> zombieEntities = new ArrayList<>();
        for (Zombie z : waveZombies) {
            if (z.isAlive() && z.getHealthPoints() > 0) zombieEntities.add(z);
        }

        ArrayList<Entity> validTargets = CombatRules.getValidTargets(defense, zombieEntities);
        if (validTargets.isEmpty()) return;

        if (defense.isExplosive()) {
            for (Entity t : validTargets) {
                if (CombatRules.shouldExplode(defense, t)) {
                    explodeDefense(defense, validTargets);
                    return;
                }
            }
        }

        int damage = getEntityDamage(defense);
        boolean attackPerformed = false;

        if (defense.hasMultipleAttacks()) {
            DefenseMultipleAttack multiDef = (DefenseMultipleAttack) defense;
            int atkCount = multiDef.getAmtOfAttacks();
            int maxTargets = Math.min(2, validTargets.size());
            ArrayList<Entity> closest = findClosestEntities(defense, validTargets, maxTargets);

            for (int i = 0; i < atkCount; i++) {
                Entity target = closest.get(i % closest.size());
                logAttack(defense, target, damage, (i + 1) + "/" + atkCount);
                if (combatLog != null) combatLog.logAttack(defense, target, damage);
                applyDamage(target, damage, defense);
                attackPerformed = true;
            }
        } else {
            int maxTargets = Math.min(2, validTargets.size());
            ArrayList<Entity> closest = findClosestEntities(defense, validTargets, maxTargets);
            for (Entity target : closest) {
                logAttack(defense, target, damage, null);
                if (combatLog != null) combatLog.logAttack(defense, target, damage);
                applyDamage(target, damage, defense);
                attackPerformed = true;
            }
        }

        if (defense.isExplosive() && attackPerformed) {
            explodeDefense(defense, validTargets);
        }
    }

    private void processZombieAttack(Zombie zombie) {
        ArrayList<Entity> defenseEntities = new ArrayList<>();
        for (Defense d : waveDefense) {
            if (d.getHealthPoints() > 0) defenseEntities.add(d);
        }
        if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
            defenseEntities.add(lifeTree);
        }

        ArrayList<Entity> validTargets = CombatRules.getValidTargets(zombie, defenseEntities);
        if (validTargets.isEmpty()) return;

        if (zombie.isExplosive()) {
            for (Entity t : validTargets) {
                if (CombatRules.shouldExplode(zombie, t)) {
                    explodeZombie(zombie, validTargets);
                    return;
                }
            }
        }

        int damage = getEntityDamage(zombie);
        int maxTargets = Math.min(2, validTargets.size());
        ArrayList<Entity> closest = findClosestEntities(zombie, validTargets, maxTargets);

        for (Entity target : closest) {
            logAttack(zombie, target, damage, null);
            if (combatLog != null) combatLog.logAttack(zombie, target, damage);
            applyDamage(target, damage, zombie);

            if (zombie.isExplosive()) {
                explodeZombie(zombie, validTargets);
                return;
            }
        }
    }

    private void processHealing(Entity healer) {
        ArrayList<Entity> candidates = new ArrayList<>();

        if (healer instanceof Defense) {
            for (Defense d : waveDefense) {
                if (d.getHealthPoints() > 0) candidates.add(d);
            }
            if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
                candidates.add(lifeTree);
            }
        } else if (healer instanceof Zombie) {
            for (Zombie z : waveZombies) {
                if (z.isAlive() && z.getHealthPoints() > 0) candidates.add(z);
            }
        }

        ArrayList<Entity> valid = CombatRules.getValidHealTargets(healer, candidates);
        if (valid.isEmpty()) return;

        Entity mostDamaged = findMostDamagedEntity(valid);
        if (mostDamaged != null) {
            int healAmount = getEntityHealPower(healer);
            if (combatLog != null) combatLog.logHeal(healer, mostDamaged, healAmount);
            applyHealing(mostDamaged, healAmount);
        }
    }

    private void explodeDefense(Defense defense, ArrayList<Entity> triggeredBy) {
        ArrayList<Entity> targets = new ArrayList<>();
        for (Zombie z : waveZombies) {
            if (z != null && z.isAlive() && z.getHealthPoints() > 0) {
                if (CombatRules.calculateDistance(defense, z) <= 1) {
                    targets.add(z);
                }
            }
        }

        if (combatLog != null) combatLog.logExplosion(defense, targets, 999999);

        for (Entity t : targets) {
            applyDamage(t, 999999, defense);
        }

        defense.setHealthPoints(0);
        removeDefenseFromBoard(defense);
    }

    private void explodeZombie(Zombie zombie, ArrayList<Entity> triggeredBy) {
        ArrayList<Entity> targets = new ArrayList<>();
        for (Defense d : waveDefense) {
            if (d != null && d.getHealthPoints() > 0) {
                if (CombatRules.calculateDistance(zombie, d) <= 1) {
                    targets.add(d);
                }
            }
        }
        if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
            if (CombatRules.calculateDistance(zombie, lifeTree) <= 1) {
                targets.add(lifeTree);
            }
        }

        if (combatLog != null) combatLog.logExplosion(zombie, targets, 999999);

        for (Entity t : targets) {
            applyDamage(t, 999999, zombie);
        }

        zombie.setHealthPoints(0);
        zombie.setAlive(false);
        if (combatLog != null) combatLog.logDeath(zombie, zombie);
    }

    private void applyDamage(Entity target, int damage, Entity attacker) {
        int cur = target.getHealthPoints();
        int next = Math.max(0, cur - damage);
        target.setHealthPoints(next);
        if (combatLog != null) combatLog.updateEntityHealth(target);

        if (next <= 0) {
            if (target instanceof Zombie z) {
                z.setAlive(false);
                if (combatLog != null && attacker != null) combatLog.logDeath(z, attacker);
            } else if (target instanceof Defense d) {
                if (combatLog != null && attacker != null) combatLog.logDeath(d, attacker);
                if (d == lifeTree) {
                    lifeTree.setHealthPoints(0);
                } else {
                    removeDefenseFromBoard(d);
                }
            }
        }
    }

    private void applyHealing(Entity target, int healAmount) {
        target.setHealthPoints(target.getHealthPoints() + healAmount);
    }

    public Zombie findClosestZombieInRange(Defense defense) {
        if (defense == null) return null;
        Zombie closest = null;
        int minDist = Integer.MAX_VALUE;
        int range = defense.getAttackRange();
        for (Zombie z : waveZombies) {
            if (z != null && z.isAlive() && z.getHealthPoints() > 0) {
                int d = CombatRules.calculateDistance(defense, z);
                if (d <= range && d < minDist) { minDist = d; closest = z; }
            }
        }
        return closest;
    }

    public Defense findClosestDefense(Zombie zombie) {
        Defense closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Defense d : waveDefense) {
            if (d != null && d.getHealthPoints() > 0) {
                int dist = CombatRules.calculateDistance(zombie, d);
                if (dist < minDist) { minDist = dist; closest = d; }
            }
        }
        return closest;
    }

    public void removeDefenseFromBoard(Defense defense) {
        int row = defense.getCurrentRow();
        int col = defense.getCurrentColumn();
        if (row >= 0 && col >= 0) {
            board.removePlacedDefense(row, col);
        }
        waveDefense.remove(defense);
    }

    private void removeDeadEntities() {
        ArrayList<Zombie> deadZombies = new ArrayList<>();
        for (Zombie z : waveZombies) {
            if (z == null) { deadZombies.add(null); continue; }
            if (z.getState() == Thread.State.NEW) continue;
            if (!z.isAlive() || z.getHealthPoints() <= 0) {
                deadZombies.add(z);
                if (combatLog != null) {
                    var stats = combatLog.getStats(z);
                    if (stats != null && !stats.died) combatLog.logDeath(z, null);
                }
            }
        }
        waveZombies.removeAll(deadZombies);

        ArrayList<Defense> deadDefenses = new ArrayList<>();
        for (Defense d : waveDefense) {
            if (d == null || d.getHealthPoints() <= 0) {
                deadDefenses.add(d);
                if (combatLog != null) {
                    var stats = combatLog.getStats(d);
                    if (stats != null && !stats.died) combatLog.logDeath(d, null);
                }
            }
        }
        waveDefense.removeAll(deadDefenses);
    }

    private int getEntityDamage(Entity entity) {
        if (entity instanceof EntityAttacker) {
            if (entity instanceof ZombieAttacker z) return z.getDamage();
            if (entity instanceof DefenseAttacker d) return d.getAttack();
        }
        return 1;
    }

    private int getEntityHealPower(Entity entity) {
        if (entity instanceof EntityHealer) {
            if (entity instanceof ZombieHealer z) return z.getHealPower();
            if (entity instanceof DefenseHealer d) return d.getHealPower();
        }
        return 5;
    }

    private ArrayList<Entity> findClosestEntities(Entity source, ArrayList<Entity> candidates, int maxCount) {
        if (candidates == null || candidates.isEmpty()) return new ArrayList<>();
        ArrayList<EntityDist> dists = new ArrayList<>();
        for (Entity c : candidates) {
            dists.add(new EntityDist(c, CombatRules.calculateDistance(source, c)));
        }
        dists.sort((a, b) -> Integer.compare(a.distance, b.distance));
        ArrayList<Entity> result = new ArrayList<>();
        for (int i = 0; i < Math.min(maxCount, dists.size()); i++) {
            result.add(dists.get(i).entity);
        }
        return result;
    }

    private Entity findMostDamagedEntity(ArrayList<Entity> candidates) {
        Entity worst = null;
        int lowest = Integer.MAX_VALUE;
        for (Entity c : candidates) {
            int hp = c.getHealthPoints();
            if (hp < lowest) { lowest = hp; worst = c; }
        }
        return worst;
    }

    private void logAttack(Entity attacker, Entity target, int damage, String tag) {
    }

    private static class EntityDist {
        Entity entity; int distance;
        EntityDist(Entity e, int d) { this.entity = e; this.distance = d; }
    }
}
