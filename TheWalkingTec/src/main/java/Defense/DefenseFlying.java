package Defense;

import java.util.ArrayList;
import java.util.Arrays;

public class DefenseFlying extends DefenseAttacker {

    public DefenseFlying(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new ArrayList<>(Arrays.asList(DefenseType.FLYING));
    }
    
    public DefenseFlying(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new ArrayList<>(types);
        this.types.add(DefenseType.FLYING);
    }

    @Override
    public void attack() {
    
    }

}
