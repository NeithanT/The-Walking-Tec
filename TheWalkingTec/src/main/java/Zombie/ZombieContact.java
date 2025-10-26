package Zombie;

public class ZombieContact extends ZombieAttacker {
    
    public ZombieContact(String name, int healthPoints, int showUpLevel, int cost, int damage) {
        super(name, healthPoints, showUpLevel, cost, damage, 1);
    }
}
