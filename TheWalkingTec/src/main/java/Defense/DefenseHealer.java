package Defense;

import Entity.EntityHealer;
import java.util.ArrayList;


public class DefenseHealer extends Defense implements EntityHealer {

    int healPower;
    
    public DefenseHealer(String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.types = new ArrayList<>();
        this.types.add(DefenseType.HEALER);
    }
    
    public DefenseHealer(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.types = new ArrayList<>(types);
        if (!this.types.contains(DefenseType.HEALER)) {
            this.types.add(DefenseType.HEALER);
        }
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
