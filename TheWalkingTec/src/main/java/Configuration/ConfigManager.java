package Configuration;

import static Configuration.FileManager.writeObject;
import Defense.Defense;
import Zombie.Zombie;
import java.util.ArrayList;

public class ConfigManager {
    
    private ArrayList<Zombie> zombies;
    private ArrayList<Defense> defenses;
    private ArrayList<Integer> gameLevels;
    
    private static final String PATH = "src/main/resources/Saves/";
    private static final String pathDefenses = PATH + "defenses.data";
    private static final String pathZombies = PATH + "zombies.data";
    private static final String pathGames = PATH + "games.data";
    
    public ConfigManager() {
        zombies = new ArrayList<>();
        defenses = new ArrayList<>();
        gameLevels = new ArrayList<>();
        
        loadDefenses();
        loadGames();
        loadZombies();
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
    
    public ArrayList<Zombie> getZombies() {
        return zombies;
    }

    public ArrayList<Defense> getDefenses() {
        return defenses;
    }

    public ArrayList<Integer> getGameLevels() {
        return gameLevels;
    }
    
    
    
    public boolean isValidZombie() {return false;}
    public boolean isValidDefense() {return false;}
    
    
}
