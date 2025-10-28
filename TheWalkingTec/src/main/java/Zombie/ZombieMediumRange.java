package Zombie;

import java.util.ArrayList;
import java.util.Arrays;

public class ZombieMediumRange extends ZombieAttacker {
    
    public ZombieMediumRange(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 0, movementSpeed); // range auto-calculated
        this.types = new ArrayList<>(Arrays.asList(ZombieType.MEDIUMRANGE));
    }
    
    public ZombieMediumRange(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 0, movementSpeed); // range auto-calculated
        this.types = new ArrayList<>(types);
        this.types.add(ZombieType.MEDIUMRANGE);
    }
    
}
