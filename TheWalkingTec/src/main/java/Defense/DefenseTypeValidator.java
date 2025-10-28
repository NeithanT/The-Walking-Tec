package Defense;

import java.util.ArrayList;

/**
 * Validates defense type combinations to prevent incompatible types
 */
public class DefenseTypeValidator {
    
    // Helper class to replace Map functionality
    private static class TypeIncompatibilityPair {
        DefenseType type;
        ArrayList<DefenseType> incompatibleTypes;
        
        TypeIncompatibilityPair(DefenseType type, ArrayList<DefenseType> incompatibleTypes) {
            this.type = type;
            this.incompatibleTypes = incompatibleTypes;
        }
    }
    
    // Define incompatible type pairs using ArrayList instead of HashMap
    private static final ArrayList<TypeIncompatibilityPair> INCOMPATIBLE_TYPES = new ArrayList<>();
    
    static {
        // BLOCKS is incompatible with everything
        ArrayList<DefenseType> blocksIncompatible = new ArrayList<>();
        blocksIncompatible.add(DefenseType.CONTACT);
        blocksIncompatible.add(DefenseType.MEDIUMRANGE);
        blocksIncompatible.add(DefenseType.FLYING);
        blocksIncompatible.add(DefenseType.EXPLOSIVE);
        blocksIncompatible.add(DefenseType.MULTIPLEATTACK);
        blocksIncompatible.add(DefenseType.HEALER);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.BLOCKS,
            blocksIncompatible
        ));
        
        // CONTACT is incompatible with EXPLOSIVE, HEALER, BLOCKS, MEDIUMRANGE
        ArrayList<DefenseType> contactIncompatible = new ArrayList<>();
        contactIncompatible.add(DefenseType.BLOCKS);
        contactIncompatible.add(DefenseType.EXPLOSIVE);
        contactIncompatible.add(DefenseType.HEALER);
        contactIncompatible.add(DefenseType.MEDIUMRANGE);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.CONTACT,
            contactIncompatible
        ));
        
        // MEDIUMRANGE is incompatible with CONTACT and BLOCKS
        ArrayList<DefenseType> mediumRangeIncompatible = new ArrayList<>();
        mediumRangeIncompatible.add(DefenseType.BLOCKS);
        mediumRangeIncompatible.add(DefenseType.CONTACT);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.MEDIUMRANGE,
            mediumRangeIncompatible
        ));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        ArrayList<DefenseType> explosiveIncompatible = new ArrayList<>();
        explosiveIncompatible.add(DefenseType.BLOCKS);
        explosiveIncompatible.add(DefenseType.CONTACT);
        explosiveIncompatible.add(DefenseType.MEDIUMRANGE);
        explosiveIncompatible.add(DefenseType.HEALER);
        explosiveIncompatible.add(DefenseType.MULTIPLEATTACK);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.EXPLOSIVE,
            explosiveIncompatible
        ));
        
        // HEALER is incompatible with EXPLOSIVE, CONTACT, BLOCKS
        ArrayList<DefenseType> healerIncompatible = new ArrayList<>();
        healerIncompatible.add(DefenseType.BLOCKS);
        healerIncompatible.add(DefenseType.EXPLOSIVE);
        healerIncompatible.add(DefenseType.CONTACT);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.HEALER,
            healerIncompatible
        ));
        
        // MULTIPLEATTACK is incompatible with HEALER, EXPLOSIVE, BLOCKS
        ArrayList<DefenseType> multipleAttackIncompatible = new ArrayList<>();
        multipleAttackIncompatible.add(DefenseType.BLOCKS);
        multipleAttackIncompatible.add(DefenseType.HEALER);
        multipleAttackIncompatible.add(DefenseType.EXPLOSIVE);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.MULTIPLEATTACK,
            multipleAttackIncompatible
        ));
        
        // FLYING is incompatible with BLOCKS only
        ArrayList<DefenseType> flyingIncompatible = new ArrayList<>();
        flyingIncompatible.add(DefenseType.BLOCKS);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.FLYING,
            flyingIncompatible
        ));
    }
    
    /**
     * Validates if a list of types is compatible
     * @param types ArrayList of defense types to validate
     * @return ValidationResult with status and error message
     */
    public static ValidationResult validate(ArrayList<DefenseType> types) {
        if (types == null || types.isEmpty()) {
            return new ValidationResult(false, "Debe seleccionar al menos un tipo");
        }
        
        // Check each type against all other types in the list
        for (DefenseType type1 : types) {
            ArrayList<DefenseType> incompatible = getIncompatibleTypesFromList(type1);
            if (incompatible != null) {
                for (DefenseType type2 : types) {
                    if (type1 != type2 && incompatible.contains(type2)) {
                        return new ValidationResult(false, 
                            String.format("%s es incompatible con %s", type1, type2));
                    }
                }
            }
        }
        
        return new ValidationResult(true, "Combinación válida");
    }
    
    /**
     * Gets the list of types incompatible with the given type
     */
    public static ArrayList<DefenseType> getIncompatibleTypes(DefenseType type) {
        return getIncompatibleTypesFromList(type);
    }
    
    /**
     * Helper method to find incompatible types from the list
     */
    private static ArrayList<DefenseType> getIncompatibleTypesFromList(DefenseType type) {
        for (TypeIncompatibilityPair pair : INCOMPATIBLE_TYPES) {
            if (pair.type == type) {
                return pair.incompatibleTypes;
            }
        }
        return new ArrayList<>();
    }
    
    /**
     * Result of type validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
