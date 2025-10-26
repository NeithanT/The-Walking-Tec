package Defense;

public class DefenseFlying extends DefenseAttacker {

    public DefenseFlying(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.type = DefenseType.FLYING;
    }

    @Override
    public void attack() {
    
    }

}
