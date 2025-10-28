package GameLogic;

import Defense.Defense;
import Table.GameBoard;
import Table.PlacedDefense;
import Table.SidePanel;
import Zombie.Zombie;
import Zombie.ZombieAttacker;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieMediumRange;
import Zombie.ZombieType;
import java.util.ArrayList;
import java.util.Random;

final class WaveManager {

    private final GameManager gameManager;

    WaveManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    static int coinsForLevel(int level) {
        return 20 + 5 * (level - 1); // Nivel 1 = 20, Nivel 2 = 25, etc.
    }
    
    static int zombiesForLevel(int level) {
        return 20 + 5 * (level - 1); // Nivel 1 = 20, Nivel 2 = 25, Nivel 3 = 30, etc.
    }

    void startRound() {
        gameManager.setWaveGenerated(false);
        gameManager.setRoundActive(false); // Desactivar hasta que se genere la oleada

        int coins = coinsForLevel(gameManager.getLevel());
        gameManager.setCoinsThisLevel(coins);
        gameManager.setDefenseCostLimit(coins);

        gameManager.getWaveZombiesInternal().clear();
        
        syncWaveDefenseWithBoard();
        
        // Initialize new combat log AFTER syncing defenses so they can be registered
        gameManager.initializeCombatLog();
        
        generateWave();
        
        // Register all zombies in combat log after generating wave
        gameManager.registerZombiesInCombatLog();
        
        gameManager.setRoundActive(true); // Activar después de generar

        SidePanel sidePanel = gameManager.getSidePanel();
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
    }

    void advanceRound() {
        gameManager.setRoundActive(false);
        gameManager.setWaveGenerated(false);
        gameManager.setLevel(gameManager.getLevel() + 1);

        int coins = coinsForLevel(gameManager.getLevel());
        gameManager.setCoinsThisLevel(coins);
        gameManager.setDefenseCostLimit(coins);

        gameManager.setZombiesRemaining(0);

        GameBoard board = gameManager.getBoard();
        if (board != null) {
            board.clearZombies();
        }

        gameManager.getWaveZombiesInternal().clear();
        
        syncWaveDefenseWithBoard();
        
        // Initialize new combat log AFTER syncing defenses so they can be registered
        gameManager.initializeCombatLog();

        if (gameManager.getSidePanel() != null) {
            gameManager.getSidePanel().appendLog("Starting round " + gameManager.getLevel());
        }
        generateWave();
        
        // Register all zombies in combat log after generating wave
        gameManager.registerZombiesInCombatLog();
        
        gameManager.setRoundActive(true);

        SidePanel sidePanel = gameManager.getSidePanel();
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
    }

    void generateWave() {
        if (gameManager.isWaveGenerated()) {
            return;
        }

        ArrayList<Zombie> pool = gameManager.getConfigManager().getZombies();
        if (gameManager.getSidePanel() != null) {
            gameManager.getSidePanel().appendLog("DEBUG: Zombie pool size: " + (pool != null ? pool.size() : "null"));
        }
        
        if (pool == null || pool.isEmpty()) {
            if (gameManager.getSidePanel() != null) {
                gameManager.getSidePanel().appendLog("No zombies configured. Cannot generate wave.");
            }
            gameManager.setWaveGenerated(true);
            return;
        }

        int level = gameManager.getLevel();
        ArrayList<Zombie> availableZombies = new ArrayList<>();
        for (Zombie zombie : pool) {
            if (zombie != null && zombie.getShowUpLevel() <= level) {
                availableZombies.add(zombie);
            }
        }

        if (availableZombies.isEmpty()) {
            for (Zombie zombie : pool) {
                if (zombie != null) {
                    availableZombies.add(zombie);
                }
            }
        }

        if (availableZombies.isEmpty()) {
            if (gameManager.getSidePanel() != null) {
                gameManager.getSidePanel().appendLog("No valid zombies found for the current round.");
            }
            gameManager.setWaveGenerated(true);
            return;
        }

        ArrayList<Zombie> waveZombies = gameManager.getWaveZombiesInternal();
        waveZombies.clear();

        // Use fixed number of zombies per level instead of budget
        int targetZombieCount = zombiesForLevel(level);
        int spawned = 0;
        gameManager.setZombiesRemaining(0);

        GameBoard board = gameManager.getBoard();
        if (board != null) {
            board.clearZombies();
        }

        Random rnd = gameManager.getRandomGenerator();
        while (spawned < targetZombieCount) {
            Zombie prototype = availableZombies.get(rnd.nextInt(availableZombies.size()));

            Zombie spawnedZombie = cloneZombie(prototype);
            if (spawnedZombie == null) {
                continue; // Skip null zombies
            }

            spawnedZombie.setGameManager(gameManager);
            spawnedZombie.setAlive(true);
            
            // Apply level scaling to the zombie
            gameManager.applyZombieScaling(spawnedZombie);

            spawned++;

            waveZombies.add(spawnedZombie);

            if (board != null) {
                board.addZombie(spawnedZombie);
            }
            
            // Start zombie thread
            spawnedZombie.startThread();

            String name = spawnedZombie.getEntityName() != null ? spawnedZombie.getEntityName() : "Zombie";
            if (gameManager.getSidePanel() != null) {
                gameManager.getSidePanel().appendLog("Spawn zombie: " + name + " (#" + spawned + ")");
            }
        }

        gameManager.setZombiesRemaining(waveZombies.size());
        gameManager.setTotalZombiesInWave(waveZombies.size()); // Set the total count that won't change

        if (spawned == 0) {
            if (gameManager.getSidePanel() != null) {
                gameManager.getSidePanel().appendLog("No zombies could be spawned for this level");
            }
        } else {
            if (gameManager.getSidePanel() != null) {
                gameManager.getSidePanel().appendLog("Wave level [" + level + "] generated with " + spawned + " zombies (target: " + targetZombieCount + ")");
            }
        }

        gameManager.setWaveGenerated(true);
        gameManager.startZombieThreads();

        SidePanel sidePanel = gameManager.getSidePanel();
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
    }

    private void syncWaveDefenseWithBoard() {
        ArrayList<Defense> waveDefense = gameManager.getWaveDefenseInternal();
        waveDefense.clear();

        GameBoard board = gameManager.getBoard();
        if (board == null) {
            return;
        }

        for (PlacedDefense placedDefense : board.getDefenses()) {
            if (placedDefense != null && placedDefense.definition != null) {
                waveDefense.add(placedDefense.definition);
            }
        }
    }

    private Zombie cloneZombie(Zombie source) {
        if (source == null) {
            return null;
        }

        Zombie clone;
        if (source instanceof ZombieExplosive explosive) {
            clone = new ZombieExplosive(explosive.getEntityName(), explosive.getHealthPoints(), explosive.getShowUpLevel(), explosive.getCost(), 0, explosive.getMovementSpeed());
        } else if (source instanceof ZombieFlying flying) {
            clone = new ZombieFlying(flying.getEntityName(), flying.getHealthPoints(), flying.getShowUpLevel(), flying.getCost(), flying.getDamage(), 0, flying.getMovementSpeed());
        } else if (source instanceof ZombieMediumRange medium) {
            clone = new ZombieMediumRange(medium.getEntityName(), medium.getHealthPoints(), medium.getShowUpLevel(), medium.getCost(), medium.getDamage(), medium.getMovementSpeed());
        } else if (source instanceof ZombieContact contact) {
            clone = new ZombieContact(contact.getEntityName(), contact.getHealthPoints(), contact.getShowUpLevel(), contact.getCost(), contact.getDamage(), contact.getMovementSpeed());
        } else if (source instanceof ZombieHealer healer) {
            clone = new ZombieHealer(healer.getEntityName(), healer.getHealthPoints(), healer.getShowUpLevel(), healer.getCost(), healer.getHealPower(), healer.getMovementSpeed());
        } else if (source instanceof ZombieAttacker attacker) {
            clone = new ZombieAttacker(attacker.getEntityName(), attacker.getHealthPoints(), attacker.getShowUpLevel(), attacker.getCost(), attacker.getDamage(), 0, attacker.getMovementSpeed());
        } else {
            ArrayList<ZombieType> types = source.getTypes();
            clone = new Zombie(types, source.getEntityName(), source.getHealthPoints(), source.getShowUpLevel(), source.getCost(), source.getMovementSpeed());
        }

        clone.setActions(source.getActions());
        clone.setImagePath(source.getImagePath());
        clone.setTypes(source.getTypes());
        return clone;
    }
}
