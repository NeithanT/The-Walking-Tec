package Defense;

import Entity.EntityExplosive;


public class DefenseExplosive extends DefenseAttacker implements EntityExplosive {
    
    public DefenseExplosive(String name, int healthPoints, int showUpLevel, int cost, int range) {
        super(name, healthPoints, showUpLevel, cost, 10000, range);
        this.type = DefenseType.EXPLOSIVE;
    }

    @Override
    public void explode() {
    
    }

}
