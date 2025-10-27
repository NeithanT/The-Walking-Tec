package Zombie;

import Entity.EntityHealer;

public class ZombieHealer extends Zombie implements EntityHealer {

    protected int healPower;

    public ZombieHealer(String name, int healthPoints, int showUpLevel, int cost, int healingPow, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.healPower = healingPow;
        this.type = ZombieType.HEALER;
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
