
package GameLogic;

import Defense.Defense;
import Table.GameBoard;
import java.awt.event.ActionEvent;
import Table.SidePanel;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Timer;


public class GameManager {
    
    private GameBoard board;
    private SidePanel sidePanel;
    private MatrixManager matrixManager;
    private Timer gameTimer;
    private boolean isPaused;
    private int level;
    private int baseHealth;
    private Defense selectedDefense;
    private int actualSpace;
            
    public GameManager(GameBoard board, SidePanel sidePanel){
        
        this.board = board;
        this.sidePanel = sidePanel;
        this.matrixManager = new MatrixManager();
        this.isPaused = true;
        this.level = 1;
        this.baseHealth = 100;
        this.selectedDefense = null;
        this.actualSpace = 0;
    }
    
    public void startGame(){
        
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
        if (!matrixManager.placeDefense(row, column)){
            return false;
        }
        
        Image img = null;
        try {
            String path = selectedDefense.getImagePath();
            if (path != null && !path.isEmpty()){
                BufferedImage raw = ImageIO.read(new File(path));
                if (raw != null){
                    
                    int w = (int)(board.getWidth() / 25.0);
                    int h = (int)(board.getHeight() / 25.0);
                    img = raw.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
                }
                
            }
        } catch (Exception e) {
        }
        
        Table.PlacedDefense placed = new Table.PlacedDefense(selectedDefense, row, column, img);
        board.addDefense(placed);
        
        
        
        System.out.println("Defensa [" + selectedDefense.getEntityName() + "] colocada en [" + row + "][" + column + "]"); 
        selectedDefense = null;
        board.clearSelectedDefense();
        sidePanel.deselectDefense();
        
        return true;
    }
    
    public boolean removeDefences(int row, int column){
        
       // matrixManager.removeDefenses(row, column);
        //TODO: BUSCAR DEFENSA EN BOARD.GETDEFENSES() Y REMOVERLA
        board.repaint();
        
        return true;
    }
    
    public void generateWave(){
        
        //TODO: CREAR ZOMBIES SEGÚN NIVEL
        System.out.println("Wave level [" + level + "] generated");
    }
    
    public void update(){
        
        if (isPaused){
            return;
        }   
        //TODO: todo lo que pase en cada frame (mov, ataques, verificaciones de colisiones, etc)
        board.repaint();
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

    public int getLevel() {
        return level;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public Defense getSelectedDefense() {
        return selectedDefense;
    }
    
    
}
