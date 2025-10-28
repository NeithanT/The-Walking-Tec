package Zombie;

import java.util.ArrayList;

/**
 * Validates zombie type combinations to prevent incompatible types
 */
public class ZombieTypeValidator {
    
    // Helper class to replace Map functionality
    private static class TypeIncompatibilityPair {
        ZombieType type;
        ArrayList<ZombieType> incompatibleTypes;
        
        TypeIncompatibilityPair(ZombieType type, ArrayList<ZombieType> incompatibleTypes) {
            this.type = type;
            this.incompatibleTypes = incompatibleTypes;
        }
    }
    
    // Define incompatible type pairs using ArrayList instead of HashMap
    private static final ArrayList<TypeIncompatibilityPair> INCOMPATIBLE_TYPES = new ArrayList<>();
    
    static {
        // CONTACT is incompatible with EXPLOSIVE, HEALER, MEDIUMRANGE
        ArrayList<ZombieType> contactIncompatible = new ArrayList<>();
        contactIncompatible.add(ZombieType.MEDIUMRANGE);
        contactIncompatible.add(ZombieType.EXPLOSIVE);
        contactIncompatible.add(ZombieType.HEALER);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.CONTACT,
            contactIncompatible
        ));
        
        // MEDIUMRANGE is incompatible with CONTACT
        ArrayList<ZombieType> mediumRangeIncompatible = new ArrayList<>();
        mediumRangeIncompatible.add(ZombieType.CONTACT);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.MEDIUMRANGE,
            mediumRangeIncompatible
        ));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        ArrayList<ZombieType> explosiveIncompatible = new ArrayList<>();
        explosiveIncompatible.add(ZombieType.CONTACT);
        explosiveIncompatible.add(ZombieType.MEDIUMRANGE);
        explosiveIncompatible.add(ZombieType.HEALER);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.EXPLOSIVE,
            explosiveIncompatible
        ));
        
        // HEALER is incompatible with EXPLOSIVE and CONTACT
        ArrayList<ZombieType> healerIncompatible = new ArrayList<>();
        healerIncompatible.add(ZombieType.EXPLOSIVE);
        healerIncompatible.add(ZombieType.CONTACT);
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.HEALER,
            healerIncompatible
        ));
        
        // FLYING has no restrictions (can combine with any)
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.FLYING,
            new ArrayList<>()
        ));
    }
    
    /**
     * Validates if a list of types is compatible
     * @param types ArrayList of zombie types to validate
     * @return ValidationResult with status and error message
     */
    public static ValidationResult validate(ArrayList<ZombieType> types) {
        if (types == null || types.isEmpty()) {
            return new ValidationResult(false, "Debe seleccionar al menos un tipo");
        }
        
        // Check each type against all other types in the list
        for (ZombieType type1 : types) {
            ArrayList<ZombieType> incompatible = getIncompatibleTypesFromList(type1);
            if (incompatible != null) {
                for (ZombieType type2 : types) {
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
    public static ArrayList<ZombieType> getIncompatibleTypes(ZombieType type) {
        return getIncompatibleTypesFromList(type);
    }
    
    /**
     * Helper method to find incompatible types from the list
     */
    private static ArrayList<ZombieType> getIncompatibleTypesFromList(ZombieType type) {
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
