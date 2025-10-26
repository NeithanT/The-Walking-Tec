package Defense;

import Entity.EntityAttacker;

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
        this.type = type;
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
}
