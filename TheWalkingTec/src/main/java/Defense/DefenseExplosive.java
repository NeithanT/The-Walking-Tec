package Defense;

import Entity.EntityExplosive;
import java.util.ArrayList;


public class DefenseExplosive extends DefenseAttacker implements EntityExplosive {
    
    public DefenseExplosive(String name, int healthPoints, int showUpLevel, int cost, int range) {
        super(name, healthPoints, showUpLevel, cost, 10000, 0); // high damage, range auto-calculated
        this.types = new ArrayList<>();
        this.types.add(DefenseType.EXPLOSIVE);
    }
    
    public DefenseExplosive(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int range) {
        super(name, healthPoints, showUpLevel, cost, 10000, 0); // high damage, range auto-calculated
        // Ensure EXPLOSIVE is always included
        this.types = new ArrayList<>(types);
        if (!this.types.contains(DefenseType.EXPLOSIVE)) {
            this.types.add(DefenseType.EXPLOSIVE);
        }
    }

    @Override
    public void explode() {
    
    }

}
