package Zombie;

import Entity.EntityAttacker;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ZombieAttacker extends Zombie implements EntityAttacker {

    protected int damage;
    protected int range;
    
    public ZombieAttacker(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.damage = damage;
        this.range = range;
    }
    
    public ZombieAttacker(ZombieType type, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new HashSet<>(Arrays.asList(type));
    }
    
    public ZombieAttacker(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new HashSet<>(types);
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
    public int getAttackRange() {
        // Special case: EXPLOSIVE zombies always have range 1 (contact)
        if (types.contains(ZombieType.EXPLOSIVE)) {
            return 1;
        }
        // Otherwise use configured range
        return range;
    }

    @Override
    public void attack() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
