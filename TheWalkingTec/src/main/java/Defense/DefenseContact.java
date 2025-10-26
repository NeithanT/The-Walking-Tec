package Defense;

public class DefenseContact extends DefenseAttacker {
    boolean reflectsDamage;

    public DefenseContact(String name, int healthPoints, int showUpLevel, int cost, int attack) {
        super(name, healthPoints, showUpLevel, cost, attack, 1);
        this.type = DefenseType.CONTACT;
    }
}
