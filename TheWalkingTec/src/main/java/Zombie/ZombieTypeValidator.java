package Zombie;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Validates zombie type combinations to prevent incompatible types
 */
public class ZombieTypeValidator {
    
    // Helper class to replace Map functionality
    private static class TypeIncompatibilityPair {
        ZombieType type;
        List<ZombieType> incompatibleTypes;
        
        TypeIncompatibilityPair(ZombieType type, List<ZombieType> incompatibleTypes) {
            this.type = type;
            this.incompatibleTypes = incompatibleTypes;
        }
    }
    
    // Define incompatible type pairs using ArrayList instead of HashMap
    private static final List<TypeIncompatibilityPair> INCOMPATIBLE_TYPES = new ArrayList<>();
    
    static {
        // CONTACT is incompatible with EXPLOSIVE, HEALER, MEDIUMRANGE
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.CONTACT,
            new ArrayList<>(Arrays.asList(
                ZombieType.MEDIUMRANGE,
                ZombieType.EXPLOSIVE,
                ZombieType.HEALER
            ))
        ));
        
        // MEDIUMRANGE is incompatible with CONTACT
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.MEDIUMRANGE,
            new ArrayList<>(Arrays.asList(ZombieType.CONTACT))
        ));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(
                ZombieType.CONTACT,
                ZombieType.MEDIUMRANGE,
                ZombieType.HEALER
            ))
        ));
        
        // HEALER is incompatible with EXPLOSIVE and CONTACT
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.HEALER,
            new ArrayList<>(Arrays.asList(
                ZombieType.EXPLOSIVE,
                ZombieType.CONTACT
            ))
        ));
        
        // FLYING has no restrictions (can combine with any)
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            ZombieType.FLYING,
            new ArrayList<>()
        ));
    }
    
    /**
     * Validates if a list of types is compatible
     * @param types List of zombie types to validate
     * @return ValidationResult with status and error message
     */
    public static ValidationResult validate(List<ZombieType> types) {
        if (types == null || types.isEmpty()) {
            return new ValidationResult(false, "Debe seleccionar al menos un tipo");
        }
        
        // Check each type against all other types in the list
        for (ZombieType type1 : types) {
            List<ZombieType> incompatible = getIncompatibleTypesFromList(type1);
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
    public static List<ZombieType> getIncompatibleTypes(ZombieType type) {
        return getIncompatibleTypesFromList(type);
    }
    
    /**
     * Helper method to find incompatible types from the list
     */
    private static List<ZombieType> getIncompatibleTypesFromList(ZombieType type) {
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
