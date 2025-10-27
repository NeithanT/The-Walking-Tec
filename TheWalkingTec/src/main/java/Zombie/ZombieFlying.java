package Zombie;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ZombieFlying extends ZombieAttacker {

    public ZombieFlying(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new HashSet<>(Arrays.asList(ZombieType.FLYING));
    }
    
    public ZombieFlying(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.types = new HashSet<>(types);
        this.types.add(ZombieType.FLYING);
    }
    
}
