package Defense;

import Entity.EntityAttacker;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class DefenseAttacker extends Defense implements EntityAttacker {
    
    protected int attack;
    protected int range; 

    public DefenseAttacker(String name, int healthPoints, int showUpLevel, int cost, int damage, int range) {
        super(name, healthPoints, showUpLevel, cost);
        this.attack = damage;
        this.range = range;
    }
    
    public DefenseAttacker(DefenseType type, String name, int healthPoints, int showUpLevel, int cost, int damage, int range) {
        this(name, healthPoints, showUpLevel, cost, damage, range);
        this.types = new HashSet<>(Arrays.asList(type));
    }
    
    public DefenseAttacker(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range) {
        this(name, healthPoints, showUpLevel, cost, damage, range);
        this.types = new HashSet<>(types);
    }
    @Override
    public void attack() {
    
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
    
    @Override
    public int getAttackRange() {
        // Special case: EXPLOSIVE defenses always have range 1 (contact)
        if (types.contains(DefenseType.EXPLOSIVE)) {
            return 1;
        }
        // Otherwise use configured range
        return range;
    }
}
