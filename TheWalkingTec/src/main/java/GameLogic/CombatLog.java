package GameLogic;

import Entity.Entity;
import Defense.Defense;
import Zombie.Zombie;
import java.util.ArrayList;
import java.util.List;

/**
 * CombatLog - Tracks all combat events and entity statistics during a battle
 */
public class CombatLog {
    
    // Entity statistics tracking - using ArrayList instead of HashMap
    private List<EntityCombatStats> entityStats;
    
    // Combat events log
    private List<CombatEvent> combatEvents;
    
    // Battle metadata
    private int level;
    private long battleStartTime;
    private long battleEndTime;
    private boolean battleEnded;
    
    public CombatLog(int level) {
        this.level = level;
        this.entityStats = new ArrayList<>();
        this.combatEvents = new ArrayList<>();
        this.battleStartTime = System.currentTimeMillis();
        this.battleEnded = false;
    }
    
    /**
     * Find stats for an entity by key
     */
    private EntityCombatStats findStatsByKey(String key) {
        for (EntityCombatStats stats : entityStats) {
            if (stats.entityKey.equals(key)) {
                return stats;
            }
        }
        return null;
    }
    
    /**
     * Get or create stats for an entity
     */
    private EntityCombatStats getOrCreateStats(Entity entity) {
        String key = getEntityKey(entity);
        EntityCombatStats existingStats = findStatsByKey(key);
        
        if (existingStats == null) {
            EntityCombatStats stats = new EntityCombatStats();
            stats.entityName = entity.getEntityName();
            stats.displayName = entity.getDisplayName(); // Name with ID
            stats.initialHealth = entity.getHealthPoints();
            stats.currentHealth = entity.getHealthPoints();
            stats.isDefense = entity instanceof Defense;
            stats.entityKey = key;
            
            // Set position for defenses, final position for zombies (will be updated)
            stats.row = entity.getCurrentRow();
            stats.column = entity.getCurrentColumn();
            
            entityStats.add(stats);
            return stats;
        }
        return existingStats;
    }
    
    /**
     * Generate unique key for entity (use instance hash for both defenses and zombies)
     */
    private String getEntityKey(Entity entity) {
        if (entity instanceof Defense) {
            // Use identity hash to distinguish different defense instances at same position
            return "D_" + System.identityHashCode(entity) + "_" + entity.getEntityName() + 
                   "@(" + entity.getCurrentRow() + "," + entity.getCurrentColumn() + ")";
        } else {
            return "Z_" + System.identityHashCode(entity) + "_" + entity.getEntityName();
        }
    }
    
    /**
     * Log an attack event
     */
    public void logAttack(Entity attacker, Entity target, int damage) {
        EntityCombatStats attackerStats = getOrCreateStats(attacker);
        EntityCombatStats targetStats = getOrCreateStats(target);
        
        // Capture HP BEFORE damage (directly from entity)
        int healthBefore = target.getHealthPoints();
        
        // Update attacker stats
        attackerStats.totalDamageDealt += damage;
        attackerStats.attacksMade++;
        attackerStats.targetsAttacked.add(getEntityKey(target));
        
        // Update target stats
        targetStats.damageReceived += damage;
        targetStats.attacksReceived++;
        targetStats.attackedBy.add(getEntityKey(attacker));
        
        // Log event
        CombatEvent event = new CombatEvent();
        event.type = CombatEventType.ATTACK;
        event.attackerKey = getEntityKey(attacker);
        event.attackerName = attacker.getDisplayName(); // Use display name with ID
        event.targetKey = getEntityKey(target);
        event.targetName = target.getDisplayName(); // Use display name with ID
        event.damage = damage;
        event.targetHealthBefore = healthBefore;
        event.targetHealthAfter = Math.max(0, healthBefore - damage);
        event.timestamp = System.currentTimeMillis() - battleStartTime;
        
        combatEvents.add(event);
    }
    
    /**
     * Update entity's current health after damage/heal is applied
     * This should be called AFTER applyDamage() or applyHealing()
     */
    public void updateEntityHealth(Entity entity) {
        EntityCombatStats stats = getOrCreateStats(entity);
        stats.currentHealth = entity.getHealthPoints();
    }
    
    /**
     * Log a heal event
     */
    public void logHeal(Entity healer, Entity target, int healAmount) {
        EntityCombatStats healerStats = getOrCreateStats(healer);
        EntityCombatStats targetStats = getOrCreateStats(target);
        
        // Update healer stats
        healerStats.totalHealingDone += healAmount;
        healerStats.healsMade++;
        healerStats.entitiesHealed.add(getEntityKey(target));
        
        // Update target stats
        targetStats.healingReceived += healAmount;
        int oldHealth = targetStats.currentHealth;
        targetStats.currentHealth = target.getHealthPoints();
        
        // Log event
        CombatEvent event = new CombatEvent();
        event.type = CombatEventType.HEAL;
        event.attackerKey = getEntityKey(healer);
        event.attackerName = healer.getDisplayName(); // Use display name with ID
        event.targetKey = getEntityKey(target);
        event.targetName = target.getDisplayName(); // Use display name with ID
        event.damage = healAmount; // Use damage field for heal amount
        event.targetHealthBefore = oldHealth;
        event.targetHealthAfter = targetStats.currentHealth;
        event.timestamp = System.currentTimeMillis() - battleStartTime;
        
        combatEvents.add(event);
    }
    
    /**
     * Log an explosion event
     */
    public void logExplosion(Entity explosive, List<Entity> targets, int damagePerTarget) {
        EntityCombatStats explosiveStats = getOrCreateStats(explosive);
        
        // Update explosive stats
        explosiveStats.explosions++;
        
        // Log event for each target
        for (Entity target : targets) {
            EntityCombatStats targetStats = getOrCreateStats(target);
            
            explosiveStats.totalDamageDealt += damagePerTarget;
            explosiveStats.targetsAttacked.add(getEntityKey(target));
            
            targetStats.damageReceived += damagePerTarget;
            int oldHealth = targetStats.currentHealth;
            targetStats.currentHealth = target.getHealthPoints();
            targetStats.attackedBy.add(getEntityKey(explosive));
            
            CombatEvent event = new CombatEvent();
            event.type = CombatEventType.EXPLOSION;
            event.attackerKey = getEntityKey(explosive);
            event.attackerName = explosive.getDisplayName(); // Use display name with ID
            event.targetKey = getEntityKey(target);
            event.targetName = target.getDisplayName(); // Use display name with ID
            event.damage = damagePerTarget;
            event.targetHealthBefore = oldHealth;
            event.targetHealthAfter = targetStats.currentHealth;
            event.timestamp = System.currentTimeMillis() - battleStartTime;
            
            combatEvents.add(event);
        }
    }
    
    /**
     * Log entity death
     */
    public void logDeath(Entity entity, Entity killer) {
        EntityCombatStats stats = getOrCreateStats(entity);
        stats.died = true;
        stats.finalHealth = 0;
        stats.deathTime = System.currentTimeMillis() - battleStartTime;
        
        if (killer != null) {
            stats.killedBy = getEntityKey(killer);
            EntityCombatStats killerStats = getOrCreateStats(killer);
            killerStats.kills++;
        }
        
        // Update final position for zombies
        if (entity instanceof Zombie) {
            stats.finalRow = entity.getCurrentRow();
            stats.finalColumn = entity.getCurrentColumn();
        }
        
        // Log event
        CombatEvent event = new CombatEvent();
        event.type = CombatEventType.DEATH;
        event.targetKey = getEntityKey(entity);
        event.targetName = entity.getDisplayName(); // Use display name with ID
        if (killer != null) {
            event.attackerKey = getEntityKey(killer);
            event.attackerName = killer.getDisplayName(); // Use display name with ID
        }
        event.timestamp = System.currentTimeMillis() - battleStartTime;
        
        combatEvents.add(event);
    }
    
    public void endBattle() {
        this.battleEnded = true;
        this.battleEndTime = System.currentTimeMillis();
        
        // Update final health for all living entities
        for (EntityCombatStats stats : entityStats) {
            if (!stats.died) {
                stats.finalHealth = stats.currentHealth;
            }
        }
    }
    
    /**
     * Mark all zombies or defenses as dead (for victory/defeat scenarios)
     */
    public void markRemainingEntitiesDead(boolean markZombies) {
        for (EntityCombatStats stats : entityStats) {
            // If marking zombies dead (victory) or marking defenses dead (defeat)
            if ((markZombies && !stats.isDefense) || (!markZombies && stats.isDefense)) {
                if (!stats.died && stats.currentHealth > 0) {
                    // Mark as died without a killer
                    stats.died = true;
                    stats.finalHealth = 0;
                    stats.deathTime = System.currentTimeMillis() - battleStartTime;
                    stats.killedBy = null;
                }
            }
        }
    }
    
    /**
     * Get stats for a specific entity (creates if doesn't exist)
     */
    public EntityCombatStats getStats(Entity entity) {
        return getOrCreateStats(entity);
    }
    
    /**
     * Get all entity stats (returns ArrayList as a values collection via wrapper)
     */
    public List<EntityCombatStats> getAllStats() {
        return entityStats;
    }
    
    /**
     * Find entity stats by entity key
     */
    public EntityCombatStats getStatsByKey(String entityKey) {
        return findStatsByKey(entityKey);
    }
    
    /**
     * Get all combat events
     */
    public List<CombatEvent> getCombatEvents() {
        return combatEvents;
    }
    
    /**
     * Get battle duration in seconds
     */
    public long getBattleDuration() {
        if (battleEnded) {
            return (battleEndTime - battleStartTime) / 1000;
        }
        return (System.currentTimeMillis() - battleStartTime) / 1000;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Entity combat statistics
     */
    public static class EntityCombatStats {
        public String entityKey;
        public String entityName;
        public String displayName; // Name with ID (e.g., "Cell Drip #5")
        public boolean isDefense;
        
        // Health tracking
        public int initialHealth;
        public int currentHealth;
        public int finalHealth;
        
        // Position tracking
        public int row;
        public int column;
        public int finalRow;
        public int finalColumn;
        
        // Combat statistics
        public int totalDamageDealt;
        public int damageReceived;
        public int totalHealingDone;
        public int healingReceived;
        public int attacksMade;
        public int attacksReceived;
        public int healsMade;
        public int kills;
        public int explosions;
        
        // Death information
        public boolean died;
        public String killedBy;
        public long deathTime;
        
        // Interaction tracking
        public List<String> targetsAttacked = new ArrayList<>();
        public List<String> attackedBy = new ArrayList<>();
        public List<String> entitiesHealed = new ArrayList<>();
        
        /**
         * Calculate hits per second
         */
        public double getHitsPerSecond(long battleDuration) {
            if (battleDuration == 0) return 0;
            return (double) attacksMade / battleDuration;
        }
        
        /**
         * Calculate damage per second
         */
        public double getDamagePerSecond(long battleDuration) {
            if (battleDuration == 0) return 0;
            return (double) totalDamageDealt / battleDuration;
        }
    }
    
    /**
     * Combat event types
     */
    public enum CombatEventType {
        ATTACK,
        HEAL,
        EXPLOSION,
        DEATH
    }
    
    /**
     * Individual combat event
     */
    public static class CombatEvent {
        public CombatEventType type;
        public String attackerKey;
        public String attackerName;
        public String targetKey;
        public String targetName;
        public int damage;
        public int targetHealthBefore;
        public int targetHealthAfter;
        public long timestamp; // milliseconds since battle start
    }
}
