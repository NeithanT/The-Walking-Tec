package Defense;

import java.util.ArrayList;

public class DefenseFlying extends DefenseAttacker {

    public DefenseFlying(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new ArrayList<>();
        this.types.add(DefenseType.FLYING);
    }
    
    public DefenseFlying(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new ArrayList<>(types);
        if (!this.types.contains(DefenseType.FLYING)) {
            this.types.add(DefenseType.FLYING);
        }
    }

    @Override
    public void attack() {
    
    }

}
