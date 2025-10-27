package Zombie;

import Entity.EntityExplosive;

public class ZombieExplosive extends ZombieAttacker implements EntityExplosive {

    
    public ZombieExplosive(String name, int healthPoints, int showUpLevel, int cost, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, 10000, range, movementSpeed);
        this.type = ZombieType.EXPLOSIVE;
    }
    
    @Override
    public void explode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void attack() {
    
    }
    
}
