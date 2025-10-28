package Zombie;

import java.util.ArrayList;

public class ZombieMediumRange extends ZombieAttacker {
    
    public ZombieMediumRange(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 0, movementSpeed); // range auto-calculated
        this.types = new ArrayList<>();
        this.types.add(ZombieType.MEDIUMRANGE);
    }
    
    public ZombieMediumRange(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 0, movementSpeed); // range auto-calculated
        this.types = new ArrayList<>(types);
        if (!this.types.contains(ZombieType.MEDIUMRANGE)) {
            this.types.add(ZombieType.MEDIUMRANGE);
        }
    }
    
}
