package Zombie;

import java.util.ArrayList;
import java.util.Arrays;

public class ZombieContact extends ZombieAttacker {
    
    public ZombieContact(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 1, movementSpeed);
        this.types = new ArrayList<>(Arrays.asList(ZombieType.CONTACT));
    }
    
    public ZombieContact(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 1, movementSpeed);
        this.types = new ArrayList<>(types);
        this.types.add(ZombieType.CONTACT);
    }
}
