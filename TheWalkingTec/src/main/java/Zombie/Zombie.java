package Zombie;

import Entity.Entity;

public class Zombie extends Entity {
    
    protected int damage;
    protected int range;
    protected ZombieType type;
    
    public Zombie() {}
    public Zombie(String name, int healthPoints, int damage, int showUpLevel, int cost, int range) {
        this.name = name;
        this.healthPoints = healthPoints;
        this.damage = damage;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        this.range = range;
        this.type = ZombieType.CONTACT;
    }
    
    // Getters
    public int getDamage() { return damage; }
    public int getRange() { return range; }
    public ZombieType getType() { return type; }
    
}
