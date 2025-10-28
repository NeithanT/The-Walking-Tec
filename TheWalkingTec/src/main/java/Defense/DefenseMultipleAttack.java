package Defense;

import java.util.ArrayList;


public class DefenseMultipleAttack extends DefenseAttacker {

    int amtOfAttacks;
    
    public DefenseMultipleAttack(String name, int healthPoints, int showUpLevel, int cost, int attack, int range, int amtOfAttacks) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new ArrayList<>();
        this.types.add(DefenseType.MULTIPLEATTACK);
        this.amtOfAttacks = amtOfAttacks;
    }
    
    public DefenseMultipleAttack(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int attack, int range, int amtOfAttacks) {
        super(name, healthPoints, showUpLevel, cost, attack, range);
        this.types = new ArrayList<>(types);
        if (!this.types.contains(DefenseType.MULTIPLEATTACK)) {
            this.types.add(DefenseType.MULTIPLEATTACK);
        }
        this.amtOfAttacks = amtOfAttacks;
    }

    public int getAmtOfAttacks() {
        return amtOfAttacks;
    }
    
    public void setAmtOfAttacks(int amtOfAttacks) {
        this.amtOfAttacks = amtOfAttacks;
    }
}
