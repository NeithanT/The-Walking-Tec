package Zombie;

import Entity.EntityExplosive;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ZombieExplosive extends ZombieAttacker implements EntityExplosive {

    
    public ZombieExplosive(String name, int healthPoints, int showUpLevel, int cost, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, 10000, range, movementSpeed);
        this.types = new HashSet<>(Arrays.asList(ZombieType.EXPLOSIVE));
    }
    
    public ZombieExplosive(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, 10000, range, movementSpeed);
        this.types = new HashSet<>(types);
        this.types.add(ZombieType.EXPLOSIVE);
    }
    
    @Override
    public void explode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void attack() {
    
    }
    
}
