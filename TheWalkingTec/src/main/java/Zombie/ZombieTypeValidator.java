package Zombie;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Validates zombie type combinations to prevent incompatible types
 */
public class ZombieTypeValidator {
    
    // Define incompatible type pairs using parallel ArrayLists
    private static final ArrayList<ZombieType> INCOMPATIBLE_KEYS = new ArrayList<>();
    private static final ArrayList<ArrayList<ZombieType>> INCOMPATIBLE_VALUES = new ArrayList<>();
    
    static {
        // CONTACT is incompatible with EXPLOSIVE, HEALER, MEDIUMRANGE
        INCOMPATIBLE_KEYS.add(ZombieType.CONTACT);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            ZombieType.MEDIUMRANGE,
            ZombieType.EXPLOSIVE,
            ZombieType.HEALER
        )));
        
        // MEDIUMRANGE is incompatible with CONTACT
        INCOMPATIBLE_KEYS.add(ZombieType.MEDIUMRANGE);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            ZombieType.CONTACT
        )));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        INCOMPATIBLE_KEYS.add(ZombieType.EXPLOSIVE);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            ZombieType.CONTACT,
            ZombieType.MEDIUMRANGE,
            ZombieType.HEALER
        )));
        
        // HEALER is incompatible with EXPLOSIVE and CONTACT
        INCOMPATIBLE_KEYS.add(ZombieType.HEALER);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            ZombieType.EXPLOSIVE,
            ZombieType.CONTACT
        )));
        
        // FLYING has no restrictions (can combine with any)
        INCOMPATIBLE_KEYS.add(ZombieType.FLYING);
        INCOMPATIBLE_VALUES.add(new ArrayList<>());
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
            ArrayList<ZombieType> incompatible = getIncompatibleTypes(type1);
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
        int index = INCOMPATIBLE_KEYS.indexOf(type);
        if (index >= 0) {
            return INCOMPATIBLE_VALUES.get(index);
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
