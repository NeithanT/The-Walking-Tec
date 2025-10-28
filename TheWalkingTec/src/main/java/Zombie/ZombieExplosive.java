package Zombie;

import Entity.EntityExplosive;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ZombieExplosive extends ZombieAttacker implements EntityExplosive {

    
    public ZombieExplosive(String name, int healthPoints, int showUpLevel, int cost, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, 10000, 0, movementSpeed); // high damage, range auto-calculated
        this.types = new ArrayList<>(Arrays.asList(ZombieType.EXPLOSIVE));
    }
    
    public ZombieExplosive(List<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, int range, double movementSpeed) {
        super(name, healthPoints, showUpLevel, cost, 10000, 0, movementSpeed); // high damage, range auto-calculated
        this.types = new ArrayList<>(types);
        if (!this.types.contains(ZombieType.EXPLOSIVE)) {
            this.types.add(ZombieType.EXPLOSIVE);
        }
    }
    
    @Override
    public void explode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void attack() {
    
    }
    
}
