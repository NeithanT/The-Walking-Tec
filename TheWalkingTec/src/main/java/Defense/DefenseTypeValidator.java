package Defense;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Validates defense type combinations to prevent incompatible types
 */
public class DefenseTypeValidator {
    
    // Define incompatible type pairs using parallel ArrayLists
    private static final ArrayList<DefenseType> INCOMPATIBLE_KEYS = new ArrayList<>();
    private static final ArrayList<ArrayList<DefenseType>> INCOMPATIBLE_VALUES = new ArrayList<>();
    
    static {
        // BLOCKS is incompatible with everything
        INCOMPATIBLE_KEYS.add(DefenseType.BLOCKS);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.CONTACT,
            DefenseType.MEDIUMRANGE,
            DefenseType.FLYING,
            DefenseType.EXPLOSIVE,
            DefenseType.MULTIPLEATTACK,
            DefenseType.HEALER
        )));
        
        // CONTACT is incompatible with EXPLOSIVE, HEALER, BLOCKS, MEDIUMRANGE
        INCOMPATIBLE_KEYS.add(DefenseType.CONTACT);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.EXPLOSIVE,
            DefenseType.HEALER,
            DefenseType.MEDIUMRANGE
        )));
        
        // MEDIUMRANGE is incompatible with CONTACT and BLOCKS
        INCOMPATIBLE_KEYS.add(DefenseType.MEDIUMRANGE);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.CONTACT
        )));
        
        // EXPLOSIVE is only compatible with FLYING (incompatible with all others)
        INCOMPATIBLE_KEYS.add(DefenseType.EXPLOSIVE);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.CONTACT,
            DefenseType.MEDIUMRANGE,
            DefenseType.HEALER,
            DefenseType.MULTIPLEATTACK
        )));
        
        // HEALER is incompatible with EXPLOSIVE, CONTACT, BLOCKS
        INCOMPATIBLE_KEYS.add(DefenseType.HEALER);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.EXPLOSIVE,
            DefenseType.CONTACT
        )));
        
        // MULTIPLEATTACK is incompatible with HEALER, EXPLOSIVE, BLOCKS
        INCOMPATIBLE_KEYS.add(DefenseType.MULTIPLEATTACK);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.BLOCKS,
            DefenseType.HEALER,
            DefenseType.EXPLOSIVE
        )));
        
        // FLYING is incompatible with BLOCKS only
        INCOMPATIBLE_KEYS.add(DefenseType.FLYING);
        INCOMPATIBLE_VALUES.add(new ArrayList<>(Arrays.asList(
            DefenseType.BLOCKS
        )));
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
            ArrayList<DefenseType> incompatible = getIncompatibleTypes(type1);
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
