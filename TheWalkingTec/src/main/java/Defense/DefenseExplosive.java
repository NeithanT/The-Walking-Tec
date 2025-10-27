package Defense;

import Entity.EntityExplosive;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


public class DefenseExplosive extends DefenseAttacker implements EntityExplosive {
    
    public DefenseExplosive(String name, int healthPoints, int showUpLevel, int cost, int range) {
        super(name, healthPoints, showUpLevel, cost, 10000, range);
        this.types = new HashSet<>(Arrays.asList(DefenseType.EXPLOSIVE));
    }
    
    public DefenseExplosive(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int range) {
        super(name, healthPoints, showUpLevel, cost, 10000, range);
        // Ensure EXPLOSIVE is always included
        this.types = new HashSet<>(types);
        this.types.add(DefenseType.EXPLOSIVE);
    }

    @Override
    public void explode() {
    
    }

}
