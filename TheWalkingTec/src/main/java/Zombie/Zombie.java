package Zombie;

import Entity.Entity;

public class Zombie extends Entity {
    
    protected int movementSpeed;
    protected ZombieType type;
    
    public Zombie() {}
    
    public Zombie(String name, int healthPoints, int showUpLevel, int cost) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
    }
    
    public Zombie(ZombieType type, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.type = type;
    }
    
    public ZombieType getType() { return type; }
    
    public void setType(ZombieType type) { this.type = type; }
    
    public String getImagePath() { return imagePath; }
    
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
}
