package Defense;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


public class DefenseMediumRange extends DefenseAttacker {

    public DefenseMediumRange(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, 0); // range is auto-calculated, ignore parameter
        this.types = new HashSet<>(Arrays.asList(DefenseType.MEDIUMRANGE));
    }
    
    public DefenseMediumRange(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, 0); // range is auto-calculated, ignore parameter
        this.types = new HashSet<>(types);
        this.types.add(DefenseType.MEDIUMRANGE);
    }

}
