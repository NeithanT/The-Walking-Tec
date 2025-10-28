package Zombie;

import java.util.ArrayList;
import java.util.Arrays;

public class ZombieFlying extends ZombieAttacker {

    public ZombieFlying(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new ArrayList<>(Arrays.asList(ZombieType.FLYING));
    }
    
    public ZombieFlying(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new ArrayList<>(types);
        this.types.add(ZombieType.FLYING);
    }
    
}
