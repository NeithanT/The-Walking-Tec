package Defense;

import java.util.ArrayList;
import java.util.Arrays;

public class DefenseContact extends DefenseAttacker {
    boolean reflectsDamage;

    public DefenseContact(String name, int healthPoints, int showUpLevel, int cost, int attack) {
        super(name, healthPoints, showUpLevel, cost, attack, 0); // range is auto-calculated
        this.types = new ArrayList<>(Arrays.asList(DefenseType.CONTACT));
    }
    
    public DefenseContact(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack) {
        super(name, healthPoints, showUpLevel, cost, attack, 0); // range is auto-calculated
        this.types = new ArrayList<>(types);
        this.types.add(DefenseType.CONTACT);
    }
}
