package Defense;

import Entity.EntityHealer;


public class DefenseHealer extends Defense implements EntityHealer {

    int healPower;
    
    public DefenseHealer(String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.type = DefenseType.HEALER;
    }

    @Override
    public void heal() {
    
    }
    
    public int getHealPower() {
        return healPower;
    }
    
    public void setHealPower(int healPower) {
        this.healPower = healPower;
    }

}
