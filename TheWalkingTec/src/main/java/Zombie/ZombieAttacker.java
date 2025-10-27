package Zombie;

import Entity.EntityAttacker;

public class ZombieAttacker extends Zombie implements EntityAttacker {

    protected int damage;
    protected int range;
    
    public ZombieAttacker(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.damage = damage;
        this.range = range;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setRange(int range) {
        this.range = range;
    }

    @Override
    public void attack() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
