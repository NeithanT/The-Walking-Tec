package Configuration;

import Zombie.Zombie;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieAttacker;
import Zombie.ZombieType;
import Defense.Defense;
import Defense.DefenseAttacker;
import Defense.DefenseContact;
import Defense.DefenseExplosive;
import Defense.DefenseFlying;
import Defense.DefenseHealer;
import Defense.DefenseMediumRange;
import Defense.DefenseMultipleAttack;
import Defense.DefenseType;
import java.util.ArrayList;


public class loadDefaultConfig {

    public static void main(String args[]) {
        
        // CONTACT, MEDIUMRANGE, FLYING, EXPLOSIVE, MULTIPLEATTACK, BLOCKS, HEALER
        // CONTACT, FLYING, MEDIUMRANGE, EXPLOSIVE, HEALER
        // String name, int healthPoints, int showUpLevel, int cost, int damage
        ConfigManager manager = new ConfigManager();
        
        Zombie z1 = new ZombieContact("Locomotor", 30, 1, 1, 5, 2);
        z1.setImagePath("src/main/resources/assets/Locomotor.png");
        Zombie z2 = new ZombieExplosive("Kamikaze", 10, 1, 1, 1, 2);
        z2.setImagePath("src/main/resources/assets/Kamikaze.png");
        Zombie z3 = new ZombieFlying("Cell Drip", 25, 1, 1, 7, 3, 2);
        z3.setImagePath("src/main/resources/assets/Cell_Drip.png");
        Zombie z4 = new ZombieContact("Perro de md", 10, 1, 1, 5, 0.5);
        z4.setImagePath("src/main/resources/assets/PerroMd.png");
        Zombie z5 = new ZombieExplosive("Sneaky Golem", 60, 5, 3, 2, 0.5);
        z5.setImagePath("src/main/resources/assets/SneakyGolem.png");
        Zombie z6 = new ZombieFlying("Terminator", 35, 3, 2, 5, 4, 1.5);
        z6.setImagePath("src/main/resources/assets/Terminator.png");
        Zombie z7 = new ZombieHealer("Makima", 100, 7, 4, 15, 1);
        z7.setImagePath("src/main/resources/assets/Makima.png");
        Zombie z8 = new ZombieContact("Big Mom", 55, 2, 3, 10, 0.5);
        z8.setImagePath("src/main/resources/assets/BigMom.png");
        Zombie z9 = new ZombieExplosive("Job Application", 1000, 10, 10, 10, 0.2);
        z9.setImagePath("src/main/resources/assets/JobApplication.png");
        Zombie z10 = new ZombieFlying("Dragona del End", 70, 7, 8, 12, 3, 1.5);
        z10.setImagePath("src/main/resources/assets/DragonaDelEnd.png");
        
        // HYBRID ZOMBIE: EXPLOSIVE + FLYING (Kamikaze using ArrayList)
        ArrayList<ZombieType> kamikazeTypes = new ArrayList<>();
        kamikazeTypes.add(ZombieType.EXPLOSIVE);
        kamikazeTypes.add(ZombieType.FLYING);
        Zombie z11 = new ZombieExplosive(kamikazeTypes, "Sky Bomber", 15, 2, 2, 5, 2.5);
        z11.setImagePath("src/main/resources/assets/SkyBomber.png");
        
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
        zombies.add(z11); // Hybrid zombie
        
        manager.saveZombies(zombies);
        
        ArrayList<String> names = new ArrayList<>();
        names.add("Life Tree");
        names.add("Goku");
        names.add("carrerin");
        names.add("Bloque de madera");
        names.add("Bloque de hierro");
        names.add("Lock in Alien");
        names.add("Waguri");
        names.add("halcon milenario");
        names.add("martillo del alba");
        names.add("perro whatsapp");
        names.add("Trampa de redstone");
        names.add("Lebron James");
        
        ArrayList<Defense> defenses = new ArrayList<>();
        
        Defense d0 = new Defense(DefenseType.BLOCKS, names.get(0), 1, 1, 1);
        d0.setImagePath("src/main/resources/assets/LifeTree.png");
        defenses.add(d0);
        
        // Goku - Flying
        Defense d1 = new DefenseFlying(names.get(1), 50, 1, 1, 10, 1);
        d1.setImagePath("src/main/resources/assets/GokuxD.jpg");
        defenses.add(d1);
        
        // carrerin - Contact
        Defense d2 = new DefenseContact(names.get(2), 50, 1, 1, 10);
        d2.setImagePath("src/main/resources/assets/Carrerin.png");
        defenses.add(d2);
        
        // Bloque de madera - Blocks
        Defense d3 = new Defense(DefenseType.BLOCKS, names.get(3), 50, 1, 1);
        d3.setImagePath("src/main/resources/assets/BloqueDeMadera.jpg");
        defenses.add(d3);
        
        // Bloque de hierro - Blocks
        Defense d4 = new Defense(DefenseType.BLOCKS, names.get(4), 50, 1, 1);
        d4.setImagePath("src/main/resources/assets/BloqueDeHierro.jpg");
        defenses.add(d4);
        
        // Lock in Alien - Healer
        Defense d5 = new DefenseHealer(names.get(5), 50, 1, 1, 5);
        d5.setImagePath("src/main/resources/assets/AlienLockIn.jpg");
        defenses.add(d5);
        
        // Waguri - Explosive
        Defense d6 = new DefenseExplosive(names.get(6), 50, 1, 1, 10);
        d6.setImagePath("src/main/resources/assets/Kaoroku.jpg");
        defenses.add(d6);
        
        // halcon milenario - Flying
        Defense d7 = new DefenseFlying(names.get(7), 50, 1, 1, 10, 1);
        d7.setImagePath("src/main/resources/assets/Halcon.png");
        defenses.add(d7);
        
        // martillo del alba - MultipleAttack
        Defense d8 = new DefenseMultipleAttack(names.get(8), 50, 1, 1, 10, 1, 3);
        d8.setImagePath("src/main/resources/assets/Martillo.png");
        defenses.add(d8);
        
        // perro whatsapp - MediumRange
        Defense d9 = new DefenseMediumRange(names.get(9), 50, 1, 1, 10, 1);
        d9.setImagePath("src/main/resources/assets/PerroWhatsApp.jpg");
        defenses.add(d9);
        
        // Trampa de redstone - Explosive
        Defense d10 = new DefenseExplosive(names.get(10), 50, 1, 1, 10);
        d10.setImagePath("src/main/resources/assets/TrampaDeRedstone.png");
        defenses.add(d10);
        
        // Lebron James - Healer
        Defense d11 = new DefenseHealer(names.get(11), 50, 1, 1, 5);
        d11.setImagePath("src/main/resources/assets/Lebron_James.jpg");
        defenses.add(d11);
        
        // HYBRID DEFENSE: FLYING + HEALER (Medic Drone using ArrayList)
        ArrayList<DefenseType> medicTypes = new ArrayList<>();
        medicTypes.add(DefenseType.FLYING);
        medicTypes.add(DefenseType.HEALER);
        Defense d12 = new DefenseHealer(medicTypes, "Medic Drone", 40, 2, 3, 8);
        d12.setImagePath("src/main/resources/assets/MedicDrone.png");
        defenses.add(d12);
        
        manager.saveDefenses(defenses);
        
        
    }
}
