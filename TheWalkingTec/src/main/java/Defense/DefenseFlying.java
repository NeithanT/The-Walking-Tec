package Defense;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class DefenseFlying extends DefenseAttacker {

    public DefenseFlying(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new HashSet<>(Arrays.asList(DefenseType.FLYING));
    }
    
    public DefenseFlying(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new HashSet<>(types);
        this.types.add(DefenseType.FLYING);
    }

    @Override
    public void attack() {
    
    }

}
