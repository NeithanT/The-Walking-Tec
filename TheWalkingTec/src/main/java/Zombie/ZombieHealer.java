package Zombie;

import Entity.EntityHealer;

public class ZombieHealer extends Zombie implements EntityHealer {

    protected int healPower;

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
