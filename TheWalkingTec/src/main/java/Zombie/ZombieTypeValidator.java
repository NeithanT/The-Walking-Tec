package Zombie;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Validates zombie type combinations to prevent incompatible types
 */
public class ZombieTypeValidator {
    
    // Define incompatible type pairs
    private static final Map<ZombieType, Set<ZombieType>> INCOMPATIBLE_TYPES = new HashMap<>();
    
    static {
        // CONTACT is incompatible with EXPLOSIVE, HEALER, MEDIUMRANGE
        INCOMPATIBLE_TYPES.put(ZombieType.CONTACT, new HashSet<>(Arrays.asList(
            ZombieType.MEDIUMRANGE,
            ZombieType.EXPLOSIVE,
            ZombieType.HEALER
        )));
        
        // MEDIUMRANGE is incompatible with CONTACT
        INCOMPATIBLE_TYPES.put(ZombieType.MEDIUMRANGE, new HashSet<>(Arrays.asList(
            ZombieType.CONTACT
        )));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        INCOMPATIBLE_TYPES.put(ZombieType.EXPLOSIVE, new HashSet<>(Arrays.asList(
            ZombieType.CONTACT,
            ZombieType.MEDIUMRANGE,
            ZombieType.HEALER
        )));
        
        // HEALER is incompatible with EXPLOSIVE and CONTACT
        INCOMPATIBLE_TYPES.put(ZombieType.HEALER, new HashSet<>(Arrays.asList(
            ZombieType.EXPLOSIVE,
            ZombieType.CONTACT
        )));
        
        // FLYING has no restrictions (can combine with any)
        INCOMPATIBLE_TYPES.put(ZombieType.FLYING, new HashSet<>());
    }
    
    /**
     * Validates if a set of types is compatible
     * @param types Set of zombie types to validate
     * @return ValidationResult with status and error message
     */
    public static ValidationResult validate(Set<ZombieType> types) {
        if (types == null || types.isEmpty()) {
            return new ValidationResult(false, "Debe seleccionar al menos un tipo");
        }
        
        // Check each type against all other types in the set
        for (ZombieType type1 : types) {
            Set<ZombieType> incompatible = INCOMPATIBLE_TYPES.get(type1);
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
     * Gets the set of types incompatible with the given type
     */
    public static Set<ZombieType> getIncompatibleTypes(ZombieType type) {
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
