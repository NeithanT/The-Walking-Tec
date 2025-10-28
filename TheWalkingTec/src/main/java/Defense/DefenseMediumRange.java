package Defense;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public class DefenseMediumRange extends DefenseAttacker {

    public DefenseMediumRange(String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, 0); // range is auto-calculated, ignore parameter
        this.types = new ArrayList<>(Arrays.asList(DefenseType.MEDIUMRANGE));
    }
    
    public DefenseMediumRange(List<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack, int range) {
        super(name, healthPoints, showUpLevel, cost, attack, 0); // range is auto-calculated, ignore parameter
        this.types = new ArrayList<>(types);
        if (!this.types.contains(DefenseType.MEDIUMRANGE)) {
            this.types.add(DefenseType.MEDIUMRANGE);
        }
    }

}
