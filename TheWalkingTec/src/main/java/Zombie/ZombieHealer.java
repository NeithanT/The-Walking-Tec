package Zombie;

import Entity.EntityHealer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ZombieHealer extends Zombie implements EntityHealer {

    protected int healPower;

    public ZombieHealer(String name, int healthPoints, int showUpLevel, int cost, int healingPow, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.healPower = healingPow;
        this.types = new ArrayList<>(Arrays.asList(ZombieType.HEALER));
    }
    
    public ZombieHealer(List<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int healingPow, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.healPower = healingPow;
        this.types = new ArrayList<>(types);
        if (!this.types.contains(ZombieType.HEALER)) {
            this.types.add(ZombieType.HEALER);
        }
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
