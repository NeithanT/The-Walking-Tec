package Configuration;

import static Configuration.FileManager.writeObject;
import Defense.Defense;
import Zombie.Zombie;
import java.util.ArrayList;
import java.util.List;

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
    }
    
    public void addDefense(Defense newDefense) {
        defenses.add(newDefense);
        
        writeObject(defenses, pathDefenses);
    }
    
    public void addGame(int level) {
        gameLevels.add(level);
        writeObject(gameLevels, pathGames);
    }
    
    public void addZombie(Zombie newZombie) {
        zombies.add(newZombie);
        
        writeObject(zombies, pathZombies);
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
        writeObject(zombies, pathZombies);
    }
    
    public void removeDefense(Defense defense) {
        defenses.remove(defense);
        writeObject(defenses, pathDefenses);
    }
    
    public boolean isValidZombie() {return false;}
    public boolean isValidDefense() {return false;}
    
    public void saveZombies(List<Zombie> updatedZombies) {
        zombies = new ArrayList<>(updatedZombies);
        writeObject(zombies, pathZombies);
    }
    
    public void saveDefenses(List<Defense> updatedDefenses) {
        defenses = new ArrayList<>(updatedDefenses);
        writeObject(defenses, pathDefenses);
    }

    public void saveAdmins(List<Admin> updatedAdmins) {
        admins = new ArrayList<>(updatedAdmins);
        writeObject(admins, pathAdmins);
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
        writeObject(admins, pathAdmins);
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
                writeObject(admins, pathAdmins);
                return true;
            }
        }
        return false;
    }

    public void removeAdmin(Admin admin) {
        admins.remove(admin);
        writeObject(admins, pathAdmins);
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
            writeObject(admins, pathAdmins);
        }
    }
    
    
}
