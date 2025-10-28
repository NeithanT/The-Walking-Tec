package Test;

import Defense.DefenseType;
import Defense.DefenseTypeValidator;
import Zombie.ZombieType;
import Zombie.ZombieTypeValidator;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tests the type compatibility validation system
 */
public class TypeValidationTest {
    
    public static void main(String[] args) {
        System.out.println("=== TYPE VALIDATION SYSTEM TEST ===\n");
        
        testDefenseValidation();
        System.out.println("\n" + "=".repeat(50) + "\n");
        testZombieValidation();
        
        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
    
    private static void testDefenseValidation() {
        System.out.println("--- DEFENSE TYPE VALIDATION ---\n");
        
        // Valid combinations
        testDefense("FLYING + EXPLOSIVE (Bombardero)", 
            DefenseType.FLYING, DefenseType.EXPLOSIVE);
        
        testDefense("FLYING + HEALER (Dron Médico)", 
            DefenseType.FLYING, DefenseType.HEALER);
        
        testDefense("MEDIUMRANGE + MULTIPLEATTACK (Artillería)", 
            DefenseType.MEDIUMRANGE, DefenseType.MULTIPLEATTACK);
        
        testDefense("CONTACT + FLYING (Guerrero Aéreo)", 
            DefenseType.CONTACT, DefenseType.FLYING);
        
        // Invalid combinations
        testDefense("BLOCKS + EXPLOSIVE (INVALID)", 
            DefenseType.BLOCKS, DefenseType.EXPLOSIVE);
        
        testDefense("EXPLOSIVE + HEALER (INVALID)", 
            DefenseType.EXPLOSIVE, DefenseType.HEALER);
        
        testDefense("CONTACT + MEDIUMRANGE (INVALID)", 
            DefenseType.CONTACT, DefenseType.MEDIUMRANGE);
        
        testDefense("BLOCKS + FLYING (INVALID)", 
            DefenseType.BLOCKS, DefenseType.FLYING);
        
        // Edge cases
        testDefense("Solo BLOCKS", DefenseType.BLOCKS);
        testDefense("Solo FLYING", DefenseType.FLYING);
        testDefense("Ningún tipo", new DefenseType[0]);
    }
    
    private static void testZombieValidation() {
        System.out.println("--- ZOMBIE TYPE VALIDATION ---\n");
        
        // Valid combinations
        testZombie("FLYING + EXPLOSIVE (Kamikaze Aéreo)", 
            ZombieType.FLYING, ZombieType.EXPLOSIVE);
        
        testZombie("FLYING + HEALER (Ángel Curador)", 
            ZombieType.FLYING, ZombieType.HEALER);
        
        testZombie("CONTACT + FLYING (Melee Aéreo)", 
            ZombieType.CONTACT, ZombieType.FLYING);
        
        testZombie("MEDIUMRANGE + FLYING (Francotirador Aéreo)", 
            ZombieType.MEDIUMRANGE, ZombieType.FLYING);
        
        // Invalid combinations
        testZombie("EXPLOSIVE + HEALER (INVALID)", 
            ZombieType.EXPLOSIVE, ZombieType.HEALER);
        
        testZombie("CONTACT + MEDIUMRANGE (INVALID)", 
            ZombieType.CONTACT, ZombieType.MEDIUMRANGE);
        
        // Edge cases
        testZombie("Solo CONTACT", ZombieType.CONTACT);
        testZombie("Solo FLYING", ZombieType.FLYING);
        testZombie("Ningún tipo", new ZombieType[0]);
    }
    
    private static void testDefense(String description, DefenseType... types) {
        List<DefenseType> typeList = new ArrayList<>(Arrays.asList(types));
        DefenseTypeValidator.ValidationResult result = DefenseTypeValidator.validate(typeList);
        
        String status = result.isValid() ? "✅ VÁLIDO" : "❌ INVÁLIDO";
        System.out.printf("%-45s %s\n", description + ":", status);
        if (!result.isValid()) {
            System.out.printf("   Razón: %s\n", result.getMessage());
        }
        System.out.println();
    }
    
    private static void testZombie(String description, ZombieType... types) {
        List<ZombieType> typeList = new ArrayList<>(Arrays.asList(types));
        ZombieTypeValidator.ValidationResult result = ZombieTypeValidator.validate(typeList);
        
        String status = result.isValid() ? "✅ VÁLIDO" : "❌ INVÁLIDO";
        System.out.printf("%-45s %s\n", description + ":", status);
        if (!result.isValid()) {
            System.out.printf("   Razón: %s\n", result.getMessage());
        }
        System.out.println();
    }
}
