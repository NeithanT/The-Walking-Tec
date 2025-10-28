package Defense;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Validates defense type combinations to prevent incompatible types
 */
public class DefenseTypeValidator {
    
    // Helper class to replace Map functionality
    private static class TypeIncompatibilityPair {
        DefenseType type;
        List<DefenseType> incompatibleTypes;
        
        TypeIncompatibilityPair(DefenseType type, List<DefenseType> incompatibleTypes) {
            this.type = type;
            this.incompatibleTypes = incompatibleTypes;
        }
    }
    
    // Define incompatible type pairs using ArrayList instead of HashMap
    private static final List<TypeIncompatibilityPair> INCOMPATIBLE_TYPES = new ArrayList<>();
    
    static {
        // BLOCKS is incompatible with everything
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.BLOCKS,
            new ArrayList<>(Arrays.asList(
                DefenseType.CONTACT,
                DefenseType.MEDIUMRANGE,
                DefenseType.FLYING,
                DefenseType.EXPLOSIVE,
                DefenseType.MULTIPLEATTACK,
                DefenseType.HEALER
            ))
        ));
        
        // CONTACT is incompatible with EXPLOSIVE, HEALER, BLOCKS, MEDIUMRANGE
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.CONTACT,
            new ArrayList<>(Arrays.asList(
                DefenseType.BLOCKS,
                DefenseType.EXPLOSIVE,
                DefenseType.HEALER,
                DefenseType.MEDIUMRANGE
            ))
        ));
        
        // MEDIUMRANGE is incompatible with CONTACT and BLOCKS
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.MEDIUMRANGE,
            new ArrayList<>(Arrays.asList(
                DefenseType.BLOCKS,
                DefenseType.CONTACT
            ))
        ));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(
                DefenseType.BLOCKS,
                DefenseType.CONTACT,
                DefenseType.MEDIUMRANGE,
                DefenseType.HEALER,
                DefenseType.MULTIPLEATTACK
            ))
        ));
        
        // HEALER is incompatible with EXPLOSIVE, CONTACT, BLOCKS
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.HEALER,
            new ArrayList<>(Arrays.asList(
                DefenseType.BLOCKS,
                DefenseType.EXPLOSIVE,
                DefenseType.CONTACT
            ))
        ));
        
        // MULTIPLEATTACK is incompatible with HEALER, EXPLOSIVE, BLOCKS
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.MULTIPLEATTACK,
            new ArrayList<>(Arrays.asList(
                DefenseType.BLOCKS,
                DefenseType.HEALER,
                DefenseType.EXPLOSIVE
            ))
        ));
        
        // FLYING is incompatible with BLOCKS only
        INCOMPATIBLE_TYPES.add(new TypeIncompatibilityPair(
            DefenseType.FLYING,
            new ArrayList<>(Arrays.asList(DefenseType.BLOCKS))
        ));
    }
    
    /**
     * Validates if a list of types is compatible
     * @param types List of defense types to validate
     * @return ValidationResult with status and error message
     */
    public static ValidationResult validate(List<DefenseType> types) {
        if (types == null || types.isEmpty()) {
            return new ValidationResult(false, "Debe seleccionar al menos un tipo");
        }
        
        // Check each type against all other types in the list
        for (DefenseType type1 : types) {
            List<DefenseType> incompatible = getIncompatibleTypesFromList(type1);
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
    public static List<DefenseType> getIncompatibleTypes(DefenseType type) {
        return getIncompatibleTypesFromList(type);
    }
    
    /**
     * Helper method to find incompatible types from the list
     */
    private static List<DefenseType> getIncompatibleTypesFromList(DefenseType type) {
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
