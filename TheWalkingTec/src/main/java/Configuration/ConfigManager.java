package Configuration;

import Defense.Defense;
import Defense.DefenseAttacker;
import Defense.DefenseContact;
import Defense.DefenseExplosive;
import Defense.DefenseFlying;
import Defense.DefenseHealer;
import Defense.DefenseMediumRange;
import Defense.DefenseMultipleAttack;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import java.util.ArrayList;

public class ConfigManager {
    
    private ArrayList<Zombie> zombies;
    private ArrayList<Defense> defenses;
    private ArrayList<Integer> gameLevels;
    private ArrayList<Admin> admins;
    
    private static final String PATH = "src/main/resources/Saves/";
    private static final String pathDefenses = PATH + "defenses.data";
    private static final String pathZombies = PATH + "zombies.data";
    private static final String pathGames = PATH + "games.data";
    private static final String pathAdmins = PATH + "admins.data";
    
    public ConfigManager() {
        zombies = new ArrayList<>();
        defenses = new ArrayList<>();
        gameLevels = new ArrayList<>();
        admins = new ArrayList<>();
        
        loadDefenses();
        loadGames();
        loadZombies();
        loadAdmins();
        
        ensureDefaultAdmin();
        ensureDefaultZombies();
        ensureDefaultDefenses();
    }
    
    public void addDefense(Defense newDefense) {
        defenses.add(newDefense);
        
        FileManager.writeObject(defenses, pathDefenses);
    }
    
    public void addGame(int level) {
        gameLevels.add(level);
        FileManager.writeObject(gameLevels, pathGames);
    }
    
    public void addZombie(Zombie newZombie) {
        zombies.add(newZombie);
        
        FileManager.writeObject(zombies, pathZombies);
    }
    
    public void loadDefenses() {
        ArrayList<Defense> loaded = (ArrayList<Defense>)FileManager.readObject(pathDefenses);
        if (loaded != null) {
            defenses = loaded;
        }
    }
    
    public void loadGames() {
        ArrayList<Integer> loaded = (ArrayList<Integer>)FileManager.readObject(pathGames);
        if (loaded != null) {
            gameLevels = loaded;
        }
    }
    
    public void loadZombies() {
        ArrayList<Zombie> loaded = (ArrayList<Zombie>)FileManager.readObject(pathZombies);
        
        if (loaded != null) {
            zombies = loaded;
        }
    }

    public void loadAdmins() {
        ArrayList<Admin> loaded = (ArrayList<Admin>) FileManager.readObject(pathAdmins);
        if (loaded != null) {
            admins = loaded;
        }
    }
    
    public ArrayList<Zombie> getZombies() {
        return zombies;
    }

    public ArrayList<Defense> getDefenses() {
        return defenses;
    }

    public ArrayList<Integer> getGameLevels() {
        return gameLevels;
    }
    
    public ArrayList<Admin> getAdmins() {
        return new ArrayList<>(admins);
    }
    
    
    public void removeZombie(Zombie zombie) {
        zombies.remove(zombie);
        FileManager.writeObject(zombies, pathZombies);
    }
    
    public void removeDefense(Defense defense) {
        defenses.remove(defense);
        FileManager.writeObject(defenses, pathDefenses);
    }
    
    public boolean isValidZombie() {return false;}
    public boolean isValidDefense() {return false;}
    
    public void saveZombies(ArrayList<Zombie> updatedZombies) {
        zombies = new ArrayList<>(updatedZombies);
        FileManager.writeObject(zombies, pathZombies);
    }
    
    public void saveDefenses(ArrayList<Defense> updatedDefenses) {
        defenses = new ArrayList<>(updatedDefenses);
        FileManager.writeObject(defenses, pathDefenses);
    }

    public void saveAdmins(ArrayList<Admin> updatedAdmins) {
        admins = new ArrayList<>(updatedAdmins);
        FileManager.writeObject(admins, pathAdmins);
        ensureDefaultAdmin();
    }

    public boolean addAdmin(Admin newAdmin) {
        if (newAdmin == null || newAdmin.getUsername() == null || newAdmin.getUsername().isBlank()) {
            return false;
        }
        for (Admin existing : admins) {
            if (existing.getUsername().equalsIgnoreCase(newAdmin.getUsername())) {
                return false;
            }
        }
        admins.add(newAdmin);
        FileManager.writeObject(admins, pathAdmins);
        return true;
    }

    public boolean updateAdmin(String originalUsername, Admin updatedAdmin) {
        if (originalUsername == null || updatedAdmin == null || updatedAdmin.getUsername() == null || updatedAdmin.getUsername().isBlank()) {
            return false;
        }

        for (Admin existing : admins) {
            if (!existing.getUsername().equalsIgnoreCase(originalUsername)
                    && existing.getUsername().equalsIgnoreCase(updatedAdmin.getUsername())) {
                return false;
            }
        }

        for (int i = 0; i < admins.size(); i++) {
            Admin admin = admins.get(i);
            if (admin.getUsername().equalsIgnoreCase(originalUsername)) {
                admins.set(i, updatedAdmin);
                FileManager.writeObject(admins, pathAdmins);
                return true;
            }
        }
        return false;
    }

    public void removeAdmin(Admin admin) {
        admins.remove(admin);
        FileManager.writeObject(admins, pathAdmins);
        ensureDefaultAdmin();
    }

    public boolean authenticateAdmin(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        for (Admin admin : admins) {
            if (username.equalsIgnoreCase(admin.getUsername()) && password.equals(admin.getPassword())) {
                return true;
            }
        }
        return false;
    }

    private void ensureDefaultAdmin() {
        if (admins == null) {
            admins = new ArrayList<>();
        }
        if (admins.isEmpty()) {
            admins.add(new Admin("admin", "admin"));
            FileManager.writeObject(admins, pathAdmins);
        }
    }
    
    private void ensureDefaultZombies() {
        if (zombies == null) {
            zombies = new ArrayList<>();
        }
        if (zombies.isEmpty()) {
            loadDefaultZombies();
        }
    }
    
    private void ensureDefaultDefenses() {
        if (defenses == null) {
            defenses = new ArrayList<>();
        }
        if (defenses.isEmpty()) {
            loadDefaultDefenses();
        }
    }
    
    private void loadDefaultZombies() {
        System.out.println("Loading default zombies...");
        
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
        
        FileManager.writeObject(zombies, pathZombies);
        System.out.println("Default zombies loaded: " + zombies.size());
    }
    
    private void loadDefaultDefenses() {
        System.out.println("Loading default defenses...");
        
        Defense d0 = new Defense(DefenseType.BLOCKS, "Life Tree", 1, 1, 1);
        d0.setImagePath("src/main/resources/assets/LifeTree.png");
        defenses.add(d0);
        
        Defense d1 = new DefenseFlying("Goku", 50, 1, 1, 10, 1);
        d1.setImagePath("src/main/resources/assets/GokuxD.jpg");
        defenses.add(d1);
        
        Defense d2 = new DefenseContact("carrerin", 50, 1, 1, 10);
        d2.setImagePath("src/main/resources/assets/Carrerin.png");
        defenses.add(d2);
        
        Defense d3 = new Defense(DefenseType.BLOCKS, "Bloque de madera", 50, 1, 1);
        d3.setImagePath("src/main/resources/assets/BloqueDeMadera.jpg");
        defenses.add(d3);
        
        Defense d4 = new Defense(DefenseType.BLOCKS, "Bloque de hierro", 50, 1, 1);
        d4.setImagePath("src/main/resources/assets/BloqueDeHierro.jpg");
        defenses.add(d4);
        
        Defense d5 = new DefenseHealer("Lock in Alien", 50, 1, 1, 5);
        d5.setImagePath("src/main/resources/assets/AlienLockIn.jpg");
        defenses.add(d5);
        
        Defense d6 = new DefenseExplosive("Waguri", 50, 1, 1, 10);
        d6.setImagePath("src/main/resources/assets/Kaoroku.jpg");
        defenses.add(d6);
        
        Defense d7 = new DefenseFlying("halcon milenario", 50, 1, 1, 10, 1);
        d7.setImagePath("src/main/resources/assets/Halcon.png");
        defenses.add(d7);
        
        Defense d8 = new DefenseMultipleAttack("martillo del alba", 50, 1, 1, 10, 1, 3);
        d8.setImagePath("src/main/resources/assets/Martillo.png");
        defenses.add(d8);
        
        Defense d9 = new DefenseMediumRange("perro whatsapp", 50, 1, 1, 10, 1);
        d9.setImagePath("src/main/resources/assets/PerroWhatsApp.jpg");
        defenses.add(d9);
        
        Defense d10 = new DefenseExplosive("Trampa de redstone", 50, 1, 1, 10);
        d10.setImagePath("src/main/resources/assets/TrampaDeRedstone.png");
        defenses.add(d10);
        
        Defense d11 = new DefenseHealer("Lebron James", 50, 1, 1, 5);
        d11.setImagePath("src/main/resources/assets/Lebron_James.jpg");
        defenses.add(d11);
        
        FileManager.writeObject(defenses, pathDefenses);
        System.out.println("Default defenses loaded: " + defenses.size());
    }
    
    public void saveCurrentRound(int round) {
        if (gameLevels.isEmpty()) {
            gameLevels.add(round);
        } else {
            gameLevels.set(0, round);
        }
        FileManager.writeObject(gameLevels, pathGames);
    }

    public int getCurrentRound() {
        if (gameLevels != null && !gameLevels.isEmpty()) {
            return gameLevels.get(0);
        }
        return 1;
    }
    
}
