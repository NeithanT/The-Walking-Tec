package Zombie;

import Entity.EntityHealer;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ZombieHealer extends Zombie implements EntityHealer {

    protected int healPower;

    public ZombieHealer(String name, int healthPoints, int showUpLevel, int cost, int healingPow, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.healPower = healingPow;
        this.types = new HashSet<>(Arrays.asList(ZombieType.HEALER));
    }
    
    public ZombieHealer(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int healingPow, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.healPower = healingPow;
        this.types = new HashSet<>(types);
        this.types.add(ZombieType.HEALER);
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
