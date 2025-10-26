package Zombie;

import Entity.EntityAttacker;

public class ZombieAttacker extends Zombie implements EntityAttacker {

    protected int damage;
    protected int range;
    
    @Override
    public void attack() {

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
    
}
