package Defense;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class DefenseContact extends DefenseAttacker {
    boolean reflectsDamage;

    public DefenseContact(String name, int healthPoints, int showUpLevel, int cost, int attack) {
        super(name, healthPoints, showUpLevel, cost, attack, 1);
        this.types = new HashSet<>(Arrays.asList(DefenseType.CONTACT));
    }
    
    public DefenseContact(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack) {
        super(name, healthPoints, showUpLevel, cost, attack, 1);
        this.types = new HashSet<>(types);
        this.types.add(DefenseType.CONTACT);
    }
}
