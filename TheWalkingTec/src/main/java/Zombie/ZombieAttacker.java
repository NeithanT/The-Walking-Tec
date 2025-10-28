package Zombie;

import Entity.EntityAttacker;
import java.util.ArrayList;

public class ZombieAttacker extends Zombie implements EntityAttacker {

    protected int damage;
    
    public ZombieAttacker(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.damage = damage;
        // range parameter ignored - range is now auto-calculated based on types
    }
    
    public ZombieAttacker(ZombieType type, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new ArrayList<>();
        this.types.add(type);
    }
    
    public ZombieAttacker(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new ArrayList<>(types);
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    // getAttackRange() is inherited from Zombie class which calculates based on types
    // No override needed - use parent implementation for type-based auto-calculation

    @Override
    public void attack() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
