package Defense;


public class DefenseMediumRange extends DefenseAttacker {

    public DefenseMediumRange(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, 3);
        this.type = DefenseType.MEDIUMRANGE;
    }

}
