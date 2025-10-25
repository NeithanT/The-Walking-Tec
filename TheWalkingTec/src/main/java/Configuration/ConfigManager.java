package Configuration;

import static Configuration.FileManager.writeObject;
import Defense.Defense;
import Zombie.Zombie;
import java.util.ArrayList;

public class ConfigManager {
    
    private ArrayList<Zombie> zombies;
    private ArrayList<Defense> defenses;
    private int[] gameLevels;
    
    private static final String PATH = "src/main/resources/";
    private static final String pathDefenses = PATH + "defenses.data";
    private static final String pathZombies = PATH + "zombies.data";
    private static final String pathGames = PATH + "games.data";
    
    public ConfigManager() {
        zombies = new ArrayList<>();
        zombies.add(new Zombie("pepe", 0, 0, 0, 0, 0));
        defenses = new ArrayList<>();
        
        loadDefenses();
        loadGames();
        loadZombies();
    }
    
    public void addDefense(Defense defense) {
        writeObject(defense, pathDefenses);
    }
    
    public void addGame(int level) {
        writeObject(level, pathGames);
    }
    
    public void addZombie(Zombie newZombie) {
        writeObject(newZombie, pathZombies);
    }
    
    public void loadDefenses() {
        ArrayList<Defense> loaded = (ArrayList<Defense>)FileManager.readObject(pathDefenses);
        defenses = loaded != null ? loaded : new ArrayList<>();
    }
    
    public void loadGames() {
        gameLevels = (int[])FileManager.readObject(pathGames);
    }
    
    public void loadZombies() {
        ArrayList<Zombie> loaded = (ArrayList<Zombie>)FileManager.readObject(pathZombies);
        zombies = loaded != null ? loaded : zombies;
    }
    
    
    public boolean isValidZombie() {return false;}
    public boolean isValidDefense() {return false;}
    
    public ArrayList<Zombie> getZombies() {
        return zombies;
    }

    public ArrayList<Defense> getDefenses() {
        return defenses;
    }

    public int[] getGameLevels() {
        return gameLevels;
    }
    
}
