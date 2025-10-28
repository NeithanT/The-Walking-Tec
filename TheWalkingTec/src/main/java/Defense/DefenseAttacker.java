package Defense;

import Entity.EntityAttacker;
import java.util.ArrayList;

public class DefenseAttacker extends Defense implements EntityAttacker {
    
    protected int attack;

    public DefenseAttacker(String name, int healthPoints, int showUpLevel, int cost, int damage, int range) {
        super(name, healthPoints, showUpLevel, cost);
        this.attack = damage;
        // range parameter ignored - range is now auto-calculated based on types
    }
    
    public DefenseAttacker(DefenseType type, String name, int healthPoints, int showUpLevel, int cost, int damage, int range) {
        this(name, healthPoints, showUpLevel, cost, damage, range);
        this.types = new ArrayList<>();
        this.types.add(type);
    }
    
    public DefenseAttacker(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range) {
        this(name, healthPoints, showUpLevel, cost, damage, range);
        this.types = new ArrayList<>(types);
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
    
    // getAttackRange() is inherited from Defense class which calculates based on types
    // No override needed - use parent implementation for type-based auto-calculation
}
