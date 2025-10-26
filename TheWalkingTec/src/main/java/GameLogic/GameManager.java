
package GameLogic;

import Configuration.ConfigManager;
import Defense.Defense;
import Defense.DefenseType;
import Table.GameBoard;
import Table.PlacedDefense;
import java.awt.event.ActionEvent;
import Table.SidePanel;
import Zombie.Zombie;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.Timer;


public class GameManager {
    
    private final GameBoard board;
    private final SidePanel sidePanel;
    private final MatrixManager matrixManager;
    private Timer gameTimer;
    private boolean isPaused;
    private int level;
    private int baseHealth;
    
    private Defense selectedDefense;
    private int actualSpace;
    private int coinsThisLevel;
    private Defense lifeTree;
    private PlacedDefense lifeTreePlaced;
    private int lifeTreeRow = -1;
    private int lifeTreeColumn = -1;
    
    private final Random rnd = new Random();
    
    public GameManager(GameBoard board, SidePanel sidePanel){
        
        this.board = board;
        this.sidePanel = sidePanel;
        this.matrixManager = new MatrixManager();
        this.isPaused = true;
        this.level = 1;
        this.baseHealth = 100;
        this.selectedDefense = null;
        this.coinsThisLevel = coinsForLevel(level);
      //  this.actualSpace = 0;
    }
    
    public void startGame(){
        
        if (lifeTreePlaced == null){
            System.out.println("Place the life tree first");
            return;
        }
        if (gameTimer == null){
            int delay = 17;
            gameTimer = new Timer(delay, (ActionEvent e) -> {
                update();
            });
        }
        if (!gameTimer.isRunning()){
            gameTimer.start();
        }
        isPaused = false;
        
        coinsThisLevel = coinsForLevel(level);

        generateWave();
        System.out.println("Juego iniciado");    
    }
        
    public void pauseGame(){
        
        isPaused = !isPaused;
        String message = (isPaused ? "Juego Pausado" : "Juego reanudado");
        System.out.println(message);
    }
    
    public void stopGame(){
        if (gameTimer != null) {
            gameTimer.stop();
        }
        isPaused = true;
    }
    
    public void setSelectedDefense(Defense defenseName){
        
        this.selectedDefense = defenseName;
        board.setSelectedDefense(defenseName != null ? defenseName.getEntityName() : null);
        System.out.println("Selected Defense: " + (defenseName != null ? defenseName.getEntityName() : "null"));
        
    }
    public boolean placeDefences(int row, int column){
        
        if (selectedDefense == null){
            return false;
        }
        
        final boolean isLifeTree = "LIFE TREE".equalsIgnoreCase(selectedDefense.getEntityName());
        if (isLifeTree && lifeTreePlaced != null){
            System.out.println("Life Tree already exists");
            return false;
        }
        
        if (!matrixManager.placeDefense(row, column)){
            return false;
        }
        
        Image img = loadAndScale(selectedDefense.getImagePath());
        PlacedDefense placed = new PlacedDefense(selectedDefense, row, column, img);
        board.addDefense(placed);    
        
        
        if (isLifeTree){
            lifeTree = selectedDefense;
            lifeTreePlaced = placed;   
        }

        System.out.println("Defensa [" + selectedDefense.getEntityName() + "] colocada en [" + row + "][" + column + "]"); 
        selectedDefense = null;
        board.clearSelectedDefense();
        sidePanel.deselectDefense();
        
        return true;
    }

    public boolean removeDefences(int row, int column){
        
        if (lifeTreePlaced != null && row == lifeTreeRow && column == lifeTreeColumn){
            System.out.println("Life Tree cannot be removed");
            return false;
        }
        
        matrixManager.free(row, column);
        
        board.repaint();
        return true;
    }
    
    public void generateWave(){
        
        ConfigManager cfg = new ConfigManager();
        List<Zombie> pool = cfg.getZombies();
        if (pool == null || pool.isEmpty()) {
            return;
        }
        
        int spawnCount = Math.max(1, coinsThisLevel);
        for (int i = 0; i < spawnCount; i++){
            Zombie z = pool.get(rnd.nextInt(pool.size()));
            
         //   matrixManager.isZombieSpawnZone(row, column);
            
        
            System.out.println("Spawn zombie: " + (z.getEntityName() != null ? z.getEntityName() : "Zombie") + " #" + (i + 1));
        }
        
        
        System.out.println("Wave level [" + level + "] generated");
    }
    
    public void update(){
        
        if (isPaused){
            return;
        }   
        //TODO: todo lo que pase en cada frame (mov, ataques, verificaciones de colisiones, etc)
        board.repaint();
        
        verifyVictory();
        verifyLoss();
    }
    
    private int coinsForLevel (int lvl){
        
        return 20 + 5 * (lvl - 1);
    }
    
    public boolean isValidPlacement(int row, int column){
        
        return !matrixManager.isOccupied(row, column) && matrixManager.isValidDefensePosition(row, column);
    }
    
    public boolean isThereSpaceLeft (int totalSpace){
        
        return actualSpace < totalSpace;
    }
    
    public void verifyVictory(){
        
        if (board.getZombies().isEmpty()){
            System.out.println("Mission Completed!");
        }
        level++;        
    }
    
    public void verifyLoss(){
        
        if (baseHealth <= 0){
            System.out.println("U bad as fuck");
            stopGame();
        }
    }
    
    private Image loadAndScale (String path){
        
        if (path == null || path.isEmpty()){
            return null;
        }
        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw == null){
                return null;
            }
            int w = (int)(board.getWidth() / 25.0);
            int h = (int)(board.getHeight() / 25.0);
            if (w <= 0 || h  <= 0){
                return raw;
            }
            return raw.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
            
            
        } catch (Exception e) {
            return null;
        }
    }

    public int getLevel() {
        return level;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public Defense getSelectedDefense() {
        return selectedDefense;
    }
    public int getCoinsThisLevel(){
        return coinsThisLevel;
    }
    
}
