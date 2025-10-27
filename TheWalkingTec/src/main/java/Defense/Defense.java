package Defense;

import Entity.Entity;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class Defense extends Entity {

    protected Set<DefenseType> types;
    
    public Defense() {
       this.types = new HashSet<>(Arrays.asList(DefenseType.BLOCKS));
    }
    
    public Defense(String name, int healthPoints, int showUpLevel, int cost) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        this.types = new HashSet<>();
    }
    
    public Defense(DefenseType type, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.types = new HashSet<>(Arrays.asList(type));
    }
    
    public Defense(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.types = new HashSet<>(types);
    }
    
    public Set<DefenseType> getTypes() {
        return types;
    }
    
    public void setTypes(Set<DefenseType> types) {
        this.types = types;
    }
    
    public boolean hasType(DefenseType type) {
        return types.contains(type);
    }
    
    // Backward compatibility - returns first type or BLOCKS
    @Deprecated
    public DefenseType getType() {
        return types.isEmpty() ? DefenseType.BLOCKS : types.iterator().next();
    }
    
    @Deprecated
    public void setType(DefenseType type) {
        this.types = new HashSet<>(Arrays.asList(type));
    }
    
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getShowUpLevel() {
        return showUpLevel;
    }

    public void setShowUpLevel(int showUpLevel) {
        this.showUpLevel = showUpLevel;
    }
    
    // ==================== COMBAT CAPABILITIES IMPLEMENTATION ====================
    
    @Override
    public boolean isFlying() {
        return types.contains(DefenseType.FLYING);
    }
    
    @Override
    public boolean isExplosive() {
        return types.contains(DefenseType.EXPLOSIVE);
    }
    
    @Override
    public boolean isHealer() {
        return types.contains(DefenseType.HEALER);
    }
    
    @Override
    public int getAttackRange() {
        // Explosive always has range 1 (contact)
        if (types.contains(DefenseType.EXPLOSIVE)) {
            return 1;
        }
        
        // For multiple types, return the longest range
        int maxRange = 0;
        for (DefenseType type : types) {
            int range = getRangeForType(type);
            if (range > maxRange) {
                maxRange = range;
            }
        }
        return maxRange > 0 ? maxRange : 1;
    }
    
    private int getRangeForType(DefenseType type) {
        switch (type) {
            case CONTACT:
                return 1; // Adjacent cells only
            case MEDIUMRANGE:
                return 7; // 7x7 grid centered on defense
            case FLYING:
                return 5; // 5x5 grid for aerial defense
            case EXPLOSIVE:
                return 1; // Same as contact - needs to be close to explode
            case HEALER:
                return 7; // 7x7 healing range
            case MULTIPLEATTACK:
                return 7; // Medium range for multiple attacks
            case BLOCKS:
                return 0; // Blocks don't attack
            default:
                return 1;
        }
    }
    
    @Override
    public boolean hasMultipleAttacks() {
        return types.contains(DefenseType.MULTIPLEATTACK);
    }
}

