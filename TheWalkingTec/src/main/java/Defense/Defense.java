package Defense;

import Entity.Entity;

public class Defense extends Entity {

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
    
    DefenseType type;
    
    public Defense() {}
    public DefenseType getType() {
        return type;
    }
    
    public void setType(DefenseType type) {
        this.type = type;
    }
}
