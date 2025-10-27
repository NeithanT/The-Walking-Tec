package Entity;

import java.io.Serializable;

public abstract class Entity extends Thread implements Serializable {
    
    protected String entityName;
    protected String actions;
    protected String imagePath;
    protected int healthPoints;
    protected int cost;
    protected int showUpLevel;
    protected int currentRow;
    protected int currentColumn;
    
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
    
    public int getCurrentRow() {
        return currentRow;
    }
    
    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }
    
    public int getCurrentColumn() {
        return currentColumn;
    }
    
    public void setCurrentColumn(int currentColumn) {
        this.currentColumn = currentColumn;
    }
    
    // ==================== COMBAT CAPABILITIES ====================
    // These methods determine combat behavior based on entity type
    
    /**
     * Determines if this entity can fly (aerial unit)
     * Flying units can only be attacked by other flying units
     */
    public abstract boolean isFlying();
    
    /**
     * Determines if this entity is terrestrial (ground unit)
     */
    public boolean isTerrestrial() {
        return !isFlying();
    }
    
    /**
     * Determines if this entity is explosive (kamikaze)
     * Explosive entities deal instant kill in their explosion range
     */
    public abstract boolean isExplosive();
    
    /**
     * Determines if this entity is a healer
     * Healers cannot attack but can heal allies
     */
    public abstract boolean isHealer();
    
    /**
     * Determines if this entity can attack flying targets
     */
    public boolean canTargetFlying() {
        return isFlying(); // Only flying units can attack flying units
    }
    
    /**
     * Determines if this entity can attack ground targets
     */
    public boolean canTargetGround() {
        return isTerrestrial(); // Terrestrial units attack ground targets
    }
    
    /**
     * Gets the attack/heal range of this entity in cells
     * Returns the radius from the entity's position
     */
    public abstract int getAttackRange();
    
    /**
     * Determines if this entity has multiple attacks per action
     * Only applicable to MULTIPLEATTACK defense type
     */
    public boolean hasMultipleAttacks() {
        return false; // Override in subclasses if needed
    }
    
}
