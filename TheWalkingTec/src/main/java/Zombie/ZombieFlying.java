package Zombie;

public class ZombieFlying extends ZombieAttacker {

    public ZombieFlying(String name, int healthPoints, int showUpLevel, int cost, int damage, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, damage, range, movementSpeed);
        this.type = ZombieType.FLYING;
    }
    
}
