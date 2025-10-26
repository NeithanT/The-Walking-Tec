package Configuration;

import Zombie.Zombie;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieAttacker;
import java.util.ArrayList;


public class loadDefaultConfig {

    public static void main(String args[]) {
        
        // CONTACT, MEDIUMRANGE, FLYING, EXPLOSIVE, MULTIPLEATTACK, BLOCKS, HEALER
        // CONTACT, FLYING, MEDIUMRANGE, EXPLOSIVE, HEALER
        // String name, int healthPoints, int showUpLevel, int cost, int damage
        ConfigManager manager = new ConfigManager();
        
        Zombie z1 = new ZombieContact("Locomotor", 30, 1, 1, 5);
        Zombie z2 = new ZombieExplosive("Kamikaze", 10, 1, 1, 1);
        Zombie z3 = new ZombieFlying("Cell Drip", 25, 1, 1, 7, 3);
        Zombie z4 = new ZombieContact("Perro de md", 10, 1, 1, 5);
        Zombie z5 = new ZombieExplosive("Sneaky Golem", 60, 5, 3, 2);
        Zombie z6 = new ZombieFlying("Terminator", 35, 3, 2, 5, 4);
        Zombie z7 = new ZombieHealer("Makima", 100, 7, 4, 15);
        Zombie z8 = new ZombieContact("Big Mom", 55, 2, 3, 10);
        Zombie z9 = new ZombieExplosive("Job Application", 1000, 10, 10, 10);
        Zombie z10 = new ZombieFlying("Dragona del End", 70, 7, 8, 12, 3);
        
        ArrayList<Zombie> zombies = new ArrayList<>();
        
        zombies.add(z1);
        zombies.add(z2);
        zombies.add(z3);
        zombies.add(z4);
        zombies.add(z5);
        zombies.add(z6);
        zombies.add(z7);
        zombies.add(z8);
        zombies.add(z9);
        zombies.add(z10);
        
        manager.saveZombies(zombies);
        
        
        // Read zombies back and verify they have the same information
        ConfigManager manager2 = new ConfigManager();
        ArrayList<Zombie> loadedZombies = manager2.getZombies();
        
        System.out.println("Verifying loaded zombies:");
        for (int i = 0; i < zombies.size(); i++) {
            Zombie original = zombies.get(i);
            Zombie loaded = loadedZombies.get(i);
            
            boolean match = original.getEntityName().equals(loaded.getEntityName()) &&
                            original.getHealthPoints() == loaded.getHealthPoints() &&
                            original.getShowUpLevel() == loaded.getShowUpLevel() &&
                            original.getCost() == loaded.getCost() &&
                            original.getType() == loaded.getType();
            
            if (original instanceof ZombieAttacker && loaded instanceof ZombieAttacker) {
                ZombieAttacker origAtt = (ZombieAttacker) original;
                ZombieAttacker loadAtt = (ZombieAttacker) loaded;
                match &= origAtt.getDamage() == loadAtt.getDamage() &&
                         origAtt.getRange() == loadAtt.getRange();
            }
            
            if (original instanceof ZombieHealer && loaded instanceof ZombieHealer) {
                ZombieHealer origHeal = (ZombieHealer) original;
                ZombieHealer loadHeal = (ZombieHealer) loaded;
                match &= origHeal.getHealPower() == loadHeal.getHealPower();
            }
            
            System.out.println("Zombie " + (i+1) + ": " + (match ? "MATCH" : "MISMATCH"));
        }
        
        
    }
}
