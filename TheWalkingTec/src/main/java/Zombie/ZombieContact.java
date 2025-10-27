package Zombie;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ZombieContact extends ZombieAttacker {
    
    public ZombieContact(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 1, movementSpeed);
        this.types = new HashSet<>(Arrays.asList(ZombieType.CONTACT));
    }
    
    public ZombieContact(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 1, movementSpeed);
        this.types = new HashSet<>(types);
        this.types.add(ZombieType.CONTACT);
    }
}
