package Test;

import Defense.*;
import Zombie.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Demonstration of the Hybrid Type System
 * This shows how entities can have multiple types simultaneously
 * WITHOUT needing specific classes for each combination
 */
public class HybridTypeDemo {
    
    public static void main(String[] args) {
        System.out.println("=== HYBRID TYPE SYSTEM DEMONSTRATION ===\n");
        
        // 1. Regular single-type zombie
        ZombieFlying regularFlying = new ZombieFlying("Regular Flying", 30, 1, 1, 5, 3, 2.0);
        printZombieInfo(regularFlying);
        
        // 2. Hybrid zombie: EXPLOSIVE + FLYING using Set
        Set<ZombieType> kamikazeTypes = new HashSet<>(Arrays.asList(
            ZombieType.EXPLOSIVE, 
            ZombieType.FLYING
        ));
        Zombie kamikaze = new ZombieExplosive(kamikazeTypes, "Sky Bomber", 15, 2, 2, 5, 2.5);
        printZombieInfo(kamikaze);
        
        // 3. Regular single-type defense
        DefenseFlying regularFlyingDefense = new DefenseFlying("Goku", 50, 1, 1, 10, 5);
        printDefenseInfo(regularFlyingDefense);
        
        // 4. Hybrid defense: FLYING + HEALER using Set
        Set<DefenseType> medicTypes = new HashSet<>(Arrays.asList(
            DefenseType.FLYING, 
            DefenseType.HEALER
        ));
        Defense medicDrone = new DefenseHealer(medicTypes, "Medic Drone", 40, 2, 3, 8);
        printDefenseInfo(medicDrone);
        
        // 5. Creating custom hybrid using Set - FLYING + MULTIPLEATTACK
        Set<DefenseType> customTypes = new HashSet<>(Arrays.asList(
            DefenseType.FLYING, 
            DefenseType.MULTIPLEATTACK
        ));
        DefenseMultipleAttack customHybrid = new DefenseMultipleAttack(
            customTypes, 
            "Custom Flying Multi-Attacker", 
            60, 2, 4, 12, 5, 3
        );
        printDefenseInfo(customHybrid);
        
        System.out.println("\n=== COMBAT RULES DEMONSTRATION ===\n");
        demonstrateCombatRules(kamikaze, medicDrone);
    }
    
    private static void printZombieInfo(Zombie zombie) {
        System.out.println("--- Zombie: " + zombie.getEntityName() + " ---");
        System.out.println("Types: " + zombie.getTypes());
        System.out.println("Is Flying? " + zombie.isFlying());
        System.out.println("Is Explosive? " + zombie.isExplosive());
        System.out.println("Is Healer? " + zombie.isHealer());
        System.out.println("Attack Range: " + zombie.getAttackRange());
        System.out.println();
    }
    
    private static void printDefenseInfo(Defense defense) {
        System.out.println("--- Defense: " + defense.getEntityName() + " ---");
        System.out.println("Types: " + defense.getTypes());
        System.out.println("Is Flying? " + defense.isFlying());
        System.out.println("Is Explosive? " + defense.isExplosive());
        System.out.println("Is Healer? " + defense.isHealer());
        System.out.println("Has Multiple Attacks? " + defense.hasMultipleAttacks());
        System.out.println("Attack Range: " + defense.getAttackRange());
        System.out.println();
    }
    
    private static void demonstrateCombatRules(Zombie kamikaze, Defense medicDrone) {
        System.out.println("Sky Bomber (EXPLOSIVE + FLYING) vs Medic Drone (FLYING + HEALER):");
        System.out.println();
        
        System.out.println("1. Flying Combat:");
        System.out.println("   - Sky Bomber can attack flying targets: " + kamikaze.isFlying());
        System.out.println("   - Medic Drone can be attacked by flying zombies: " + medicDrone.isFlying());
        System.out.println("   - Result: Sky Bomber CAN attack Medic Drone (both flying)");
        System.out.println();
        
        System.out.println("2. Explosion Mechanics:");
        System.out.println("   - Sky Bomber will explode on contact: " + kamikaze.isExplosive());
        System.out.println("   - Attack range: " + kamikaze.getAttackRange() + " (must be adjacent)");
        System.out.println("   - Explosion radius: 3x3 area (affects ALL entities nearby)");
        System.out.println();
        
        System.out.println("3. Healing Capability:");
        System.out.println("   - Medic Drone can heal allies: " + medicDrone.isHealer());
        System.out.println("   - Healing range: " + medicDrone.getAttackRange());
        System.out.println();
        
        System.out.println("4. Combined Abilities:");
        System.out.println("   - Sky Bomber combines flight speed with explosive damage");
        System.out.println("   - Medic Drone can fly over zombies while healing ground defenses");
        System.out.println("   - These hybrid types create unique tactical opportunities!");
    }
}
