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
        // CONTACT is incompatible with ranged types
        INCOMPATIBLE_TYPES.put(ZombieType.CONTACT, new HashSet<>(Arrays.asList(
            ZombieType.MEDIUMRANGE  // Can't be melee and long-range
        )));
        
        // MEDIUMRANGE is incompatible with melee
        INCOMPATIBLE_TYPES.put(ZombieType.MEDIUMRANGE, new HashSet<>(Arrays.asList(
            ZombieType.CONTACT  // Can't be long-range and melee
        )));
        
        // EXPLOSIVE is incompatible with HEALER (can't heal if you explode)
        INCOMPATIBLE_TYPES.put(ZombieType.EXPLOSIVE, new HashSet<>(Arrays.asList(
            ZombieType.HEALER  // Can't heal allies if you explode
        )));
        
        // HEALER is incompatible with EXPLOSIVE
        INCOMPATIBLE_TYPES.put(ZombieType.HEALER, new HashSet<>(Arrays.asList(
            ZombieType.EXPLOSIVE  // Can't heal if you explode
        )));
        
        // FLYING can combine with all types (no restrictions)
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
