package Defense;

import Entity.EntityAttacker;

public class DefenseAttacker extends Defense implements EntityAttacker {
    
    protected int attack;
    protected int range; 

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
