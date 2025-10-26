package Zombie;

public class ZombieMediumRange extends ZombieAttacker {
    
    public ZombieMediumRange(String name, int healthPoints, int showUpLevel, int cost, int damage) {
        super(name, healthPoints, showUpLevel, cost, damage, 3);
        this.type = ZombieType.MEDIUMRANGE;
    }
    
}
