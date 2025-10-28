package Defense;

import Entity.Entity;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Defense extends Entity {

    protected List<DefenseType> types;
    
    public Defense() {
       this.types = new ArrayList<>(Arrays.asList(DefenseType.BLOCKS));
    }
    
    public Defense(String name, int healthPoints, int showUpLevel, int cost) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        this.types = new ArrayList<>();
    }
    
    public Defense(DefenseType type, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.types = new ArrayList<>(Arrays.asList(type));
    }
    
    public Defense(List<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.types = new ArrayList<>(types);
    }
    
    public List<DefenseType> getTypes() {
        return types;
    }
    
    public void setTypes(List<DefenseType> types) {
        this.types = types;
    }
    
    public boolean hasType(DefenseType type) {
        return types.contains(type);
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
        // Priority order: EXPLOSIVE > CONTACT > FLYING+MEDIUMRANGE > FLYING > MEDIUMRANGE > HEALER
        
        // EXPLOSIVE always has contact range (1) for triggering
        if (types.contains(DefenseType.EXPLOSIVE)) {
            return 1;
        }
        
        // CONTACT has range 1
        if (types.contains(DefenseType.CONTACT)) {
            return 1;
        }
        
        // FLYING + MEDIUMRANGE combination = 5x5 (radius 2)
        if (types.contains(DefenseType.FLYING) && types.contains(DefenseType.MEDIUMRANGE)) {
            return 2;
        }
        
        // FLYING alone = 5x5 (radius 2)
        if (types.contains(DefenseType.FLYING)) {
            return 2;
        }
        
        // MEDIUMRANGE = 7x7 (radius 3)
        if (types.contains(DefenseType.MEDIUMRANGE)) {
            return 3;
        }
        
        // HEALER = 7x7 (radius 3)
        if (types.contains(DefenseType.HEALER)) {
            return 3;
        }
        
        // BLOCKS and default
        return 0;
    }
    
    @Override
    public boolean hasMultipleAttacks() {
        return types.contains(DefenseType.MULTIPLEATTACK);
    }
}
