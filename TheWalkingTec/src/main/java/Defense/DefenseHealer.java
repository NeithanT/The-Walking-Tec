package Defense;

import Entity.EntityHealer;


public class DefenseHealer extends Defense implements EntityHealer {

    int healPower;
    
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
