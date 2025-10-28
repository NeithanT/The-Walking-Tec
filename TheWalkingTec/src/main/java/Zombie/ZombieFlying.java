package Zombie;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ZombieFlying extends ZombieAttacker {

    public ZombieFlying(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new ArrayList<>(Arrays.asList(ZombieType.FLYING));
    }
    
    public ZombieFlying(List<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new ArrayList<>(types);
        if (!this.types.contains(ZombieType.FLYING)) {
            this.types.add(ZombieType.FLYING);
        }
    }
    
}
