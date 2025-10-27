package Zombie;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ZombieMediumRange extends ZombieAttacker {
    
    public ZombieMediumRange(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 3, movementSpeed);
        this.types = new HashSet<>(Arrays.asList(ZombieType.MEDIUMRANGE));
    }
    
    public ZombieMediumRange(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 3, movementSpeed);
        this.types = new HashSet<>(types);
        this.types.add(ZombieType.MEDIUMRANGE);
    }
    
}
