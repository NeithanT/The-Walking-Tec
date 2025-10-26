package Defense;


public class DefenseMultipleAttack extends DefenseAttacker {

    int amtOfAttacks;
    
    public DefenseMultipleAttack(String name, int healthPoints, int showUpLevel, int cost, int attack, int range, int amtOfAttacks) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.type = DefenseType.MULTIPLEATTACK;
        this.amtOfAttacks = amtOfAttacks;
    }

    public int getAmtOfAttacks() {
        return amtOfAttacks;
    }
    
    public void setAmtOfAttacks(int amtOfAttacks) {
        this.amtOfAttacks = amtOfAttacks;
    }
}
