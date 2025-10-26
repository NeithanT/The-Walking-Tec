package Zombie;

import Entity.Entity;

public class Zombie extends Entity {
    
    protected int movementSpeed;
    protected ZombieType type;
    
    public Zombie() {}
    public Zombie(String name, int healthPoints, int damage, int showUpLevel, int cost, int range) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        this.type = ZombieType.CONTACT;
    }
    
    public ZombieType getType() { return type; }
    
    public void setType(ZombieType type) { this.type = type; }
    
}
