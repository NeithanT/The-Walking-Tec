package Defense;

import Entity.EntityHealer;
import java.util.ArrayList;
import java.util.Arrays;


public class DefenseHealer extends Defense implements EntityHealer {

    int healPower;
    
    public DefenseHealer(String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.types = new ArrayList<>(Arrays.asList(DefenseType.HEALER));
    }
    
    public DefenseHealer(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.types = new ArrayList<>(types);
        this.types.add(DefenseType.HEALER);
    }

    @Override
    public void heal() {
    
    }
    
    public int getHealPower() {
        return healPower;
    }
    
    public void setHealPower(int healPower) {
        this.healPower = healPower;
    }

}
