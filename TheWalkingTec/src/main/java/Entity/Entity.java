package Entity;

import java.io.Serializable;

public abstract class Entity extends Thread implements Serializable {
    
    protected String entityName;
    protected String actions;
    protected String imagePath;
    protected int healthPoints;
    protected int cost;
    protected int showUpLevel;
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
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
    
}
