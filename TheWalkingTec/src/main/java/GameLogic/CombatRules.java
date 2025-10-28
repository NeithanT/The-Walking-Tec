package GameLogic;

import Entity.Entity;
import Defense.Defense;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieType;
import java.util.ArrayList;

/**
 * CombatRules - Centralizes all combat logic and rules
 * 
 * Combat Rules:
 * - CONTACT: Can only attack when touching target (adjacent cell), ground only
 * - FLYING: Can attack in 5x5 range, can only attack/be attacked by other FLYING
 * - MEDIUMRANGE: Can attack in 7x7 range, ground only
 * - EXPLOSIVE: Kamikaze, seeks contact and explodes in 3x3 (zombies) or 5x5 (defenses), instant kill, ground only
 * - HEALER: Cannot attack, heals 1 ally at a time in 7x7 range, can heal both flying and ground
 * - MULTIPLEATTACK: Defenses that launch multiple attacks per action
 * - BLOCKS: Defenses with high HP to protect Life Tree, no damage
 */
public class CombatRules {
    
    /**
     * Determines if an attacker can attack a target based on combat rules
     * @param attacker The attacking entity
     * @param target The target entity
     * @return true if attack is valid
     */
    public static boolean canAttack(Entity attacker, Entity target) {
        // Dead entities can't attack or be attacked
        if (attacker.getHealthPoints() <= 0 || target.getHealthPoints() <= 0) {
            return false;
        }
        
        // Same team can't attack each other (zombie vs zombie, defense vs defense)
        if ((attacker instanceof Zombie && target instanceof Zombie) ||
            (attacker instanceof Defense && target instanceof Defense)) {
            return false;
        }
        
        // Healers can't attack
        if (attacker.isHealer()) {
            return false;
        }
        
        // Blocks can't attack
        if (attacker instanceof Defense) {
            Defense def = (Defense) attacker;
            if (def.hasType(DefenseType.BLOCKS)) {
                return false;
            }
        }
        
        // FLYING RULES:
        // FLYING + MEDIUMRANGE = ground unit (can attack and be attacked by anyone)
        // Pure FLYING = can only be attacked by other FLYING units
        // Ground units = cannot attack pure FLYING units
        
        boolean targetIsGroundUnit = isGroundUnit(target);
        boolean attackerIsGroundUnit = isGroundUnit(attacker);
        
        // Ground units cannot attack pure flying targets
        if (attackerIsGroundUnit && !targetIsGroundUnit) {
            return false;
        }
        
        // Pure flying targets can only be attacked by flying attackers
        if (!targetIsGroundUnit && attackerIsGroundUnit) {
            return false;
        }
        
        // Check if target is in range
        return isInRange(attacker, target);
    }
    
    /**
     * Determines if an entity is a ground unit
     * FLYING + MEDIUMRANGE combination is considered ground
     * Non-flying entities are ground
     * @param entity The entity to check
     * @return true if entity is ground unit
     */
    private static boolean isGroundUnit(Entity entity) {
        // FLYING + MEDIUMRANGE = ground unit
        if (entity instanceof Defense) {
            Defense def = (Defense) entity;
            if (def.hasType(DefenseType.FLYING) && def.hasType(DefenseType.MEDIUMRANGE)) {
                return true; // This combination is ground
            }
            return !def.hasType(DefenseType.FLYING); // Otherwise, flying = not ground
        } else if (entity instanceof Zombie) {
            Zombie zom = (Zombie) entity;
            if (zom.hasType(ZombieType.FLYING) && zom.hasType(ZombieType.MEDIUMRANGE)) {
                return true; // This combination is ground
            }
            return !zom.hasType(ZombieType.FLYING); // Otherwise, flying = not ground
        }
        return true; // Default to ground
    }
    
    /**
     * Determines if a healer can heal a target
     * @param healer The healing entity
     * @param target The target to heal
     * @return true if healing is valid
     */
    public static boolean canHeal(Entity healer, Entity target) {
        // Only healers can heal
        if (!healer.isHealer()) {
            return false;
        }
        
        // Dead entities can't heal or be healed
        if (healer.getHealthPoints() <= 0 || target.getHealthPoints() <= 0) {
            return false;
        }
        
        // Can't heal enemies (zombie heals zombies, defense heals defenses)
        if ((healer instanceof Zombie && !(target instanceof Zombie)) ||
            (healer instanceof Defense && !(target instanceof Defense))) {
            return false;
        }
        
        // Can't heal self
        if (healer == target) {
            return false;
        }
        
        // Target must be damaged
        // Note: We'll need to track max health for this check in the future
        
        // Check if target is in range (healers have 7x7 range)
        return isInRange(healer, target);
    }
    
    /**
     * Checks if target is within attacker's range
     * @param attacker The attacking/healing entity
     * @param target The target entity
     * @return true if target is in range
     */
    public static boolean isInRange(Entity attacker, Entity target) {
        int distance = calculateDistance(attacker, target);
        int attackRange = attacker.getAttackRange();
        
        // Range is radius in cells (Chebyshev distance / chessboard distance)
        return distance <= attackRange;
    }
    
    /**
     * Calculates the distance between two entities using Chebyshev distance
     * (maximum of absolute differences in coordinates)
     * This creates a square attack range
     * @param e1 First entity
     * @param e2 Second entity
     * @return Distance in cells
     */
    public static int calculateDistance(Entity e1, Entity e2) {
        int rowDiff = Math.abs(e1.getCurrentRow() - e2.getCurrentRow());
        int colDiff = Math.abs(e1.getCurrentColumn() - e2.getCurrentColumn());
        return Math.max(rowDiff, colDiff);
    }
    
    /**
     * Gets all valid targets for an attacker from a list of candidates
     * @param attacker The attacking entity
     * @param candidates ArrayList of potential targets
     * @return ArrayList of valid targets
     */
    public static ArrayList<Entity> getValidTargets(Entity attacker, ArrayList<Entity> candidates) {
        ArrayList<Entity> validTargets = new ArrayList<>();
        
        for (Entity candidate : candidates) {
            if (canAttack(attacker, candidate)) {
                validTargets.add(candidate);
            }
        }
        
        return validTargets;
    }
    
    /**
     * Gets all valid heal targets for a healer from a list of candidates
     * @param healer The healing entity
     * @param candidates ArrayList of potential heal targets
     * @return ArrayList of valid heal targets
     */
    public static ArrayList<Entity> getValidHealTargets(Entity healer, ArrayList<Entity> candidates) {
        ArrayList<Entity> validTargets = new ArrayList<>();
        
        for (Entity candidate : candidates) {
            if (canHeal(healer, candidate)) {
                validTargets.add(candidate);
            }
        }
        
        return validTargets;
    }
    
    /**
     * Determines if an explosive entity should explode
     * For zombies: explodes when in contact with target (distance 0)
     * For defenses: explodes when enemy enters explosion range
     * @param explosive The explosive entity
     * @param target The target
     * @return true if should explode
     */
    public static boolean shouldExplode(Entity explosive, Entity target) {
        if (!explosive.isExplosive()) {
            return false;
        }
        
        int distance = calculateDistance(explosive, target);
        
        // Zombie explosives need to be in contact (distance <= 1 means adjacent or same cell)
        if (explosive instanceof Zombie) {
            return distance <= 1;
        }
        
        // Defense explosives also need to be in contact (distance <= 1)
        // They should NOT explode from far away, only when enemy is very close
        if (explosive instanceof Defense) {
            return distance <= 1;
        }
        
        return false;
    }
}
