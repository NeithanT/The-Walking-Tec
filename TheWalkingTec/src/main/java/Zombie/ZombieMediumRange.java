package Zombie;

public class ZombieMediumRange extends ZombieAttacker {
    
    public ZombieMediumRange(String name, int healthPoints, int showUpLevel, int cost, int damage, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, 3, movementSpeed);
        this.type = ZombieType.MEDIUMRANGE;
    }
    
}
