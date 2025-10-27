package Defense;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Validates defense type combinations to prevent incompatible types
 */
public class DefenseTypeValidator {
    
    // Define incompatible type pairs
    private static final Map<DefenseType, Set<DefenseType>> INCOMPATIBLE_TYPES = new HashMap<>();
    
    static {
        // BLOCKS is incompatible with everything
        INCOMPATIBLE_TYPES.put(DefenseType.BLOCKS, new HashSet<>(Arrays.asList(
            DefenseType.CONTACT,
            DefenseType.MEDIUMRANGE,
            DefenseType.FLYING,
            DefenseType.EXPLOSIVE,
            DefenseType.MULTIPLEATTACK,
            DefenseType.HEALER
        )));
        
        // CONTACT is incompatible with EXPLOSIVE, HEALER, BLOCKS, MEDIUMRANGE
        INCOMPATIBLE_TYPES.put(DefenseType.CONTACT, new HashSet<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.EXPLOSIVE,
            DefenseType.HEALER,
            DefenseType.MEDIUMRANGE
        )));
        
        // MEDIUMRANGE is incompatible with CONTACT and BLOCKS
        INCOMPATIBLE_TYPES.put(DefenseType.MEDIUMRANGE, new HashSet<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.CONTACT
        )));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        INCOMPATIBLE_TYPES.put(DefenseType.EXPLOSIVE, new HashSet<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.CONTACT,
            DefenseType.MEDIUMRANGE,
            DefenseType.HEALER,
            DefenseType.MULTIPLEATTACK
        )));
        
        // HEALER is incompatible with EXPLOSIVE, CONTACT, BLOCKS
        INCOMPATIBLE_TYPES.put(DefenseType.HEALER, new HashSet<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.EXPLOSIVE,
            DefenseType.CONTACT
        )));
        
        // MULTIPLEATTACK is incompatible with HEALER, EXPLOSIVE, BLOCKS
        INCOMPATIBLE_TYPES.put(DefenseType.MULTIPLEATTACK, new HashSet<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.HEALER,
            DefenseType.EXPLOSIVE
        )));
        
        // FLYING is incompatible with BLOCKS only
        INCOMPATIBLE_TYPES.put(DefenseType.FLYING, new HashSet<>(Arrays.asList(
            DefenseType.BLOCKS
        )));
    }
    
    /**
     * Validates if a set of types is compatible
     * @param types Set of defense types to validate
     * @return ValidationResult with status and error message
     */
    public static ValidationResult validate(Set<DefenseType> types) {
        if (types == null || types.isEmpty()) {
            return new ValidationResult(false, "Debe seleccionar al menos un tipo");
        }
        
        // Check each type against all other types in the set
        for (DefenseType type1 : types) {
            Set<DefenseType> incompatible = INCOMPATIBLE_TYPES.get(type1);
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
     * Gets the set of types incompatible with the given type
     */
    public static Set<DefenseType> getIncompatibleTypes(DefenseType type) {
        return INCOMPATIBLE_TYPES.getOrDefault(type, new HashSet<>());
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
