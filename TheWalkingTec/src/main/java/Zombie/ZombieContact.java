package Zombie;

import java.util.ArrayList;

public class ZombieContact extends ZombieAttacker {
    
    public ZombieContact(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 1, movementSpeed);
        this.types = new ArrayList<>();
        this.types.add(ZombieType.CONTACT);
    }
    
    public ZombieContact(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 1, movementSpeed);
        this.types = new ArrayList<>(types);
        if (!this.types.contains(ZombieType.CONTACT)) {
            this.types.add(ZombieType.CONTACT);
        }
    }
}
