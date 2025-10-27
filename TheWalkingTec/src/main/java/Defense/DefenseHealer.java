package Defense;

import Entity.EntityHealer;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


public class DefenseHealer extends Defense implements EntityHealer {

    int healPower;
    
    public DefenseHealer(String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.types = new HashSet<>(Arrays.asList(DefenseType.HEALER));
    }
    
    public DefenseHealer(Set<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost, int healPower) {
        super(name, healthPoints, showUpLevel, cost);
        this.healPower = healPower;
        this.types = new HashSet<>(types);
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
