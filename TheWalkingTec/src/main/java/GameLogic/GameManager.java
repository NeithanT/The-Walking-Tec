
package GameLogic;

import Table.GameBoard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class GameManager {
    
    private GameBoard board;
    private MatrixManager matrixManager;
    private Timer gameTimer;
    private boolean isPaused;
    private int level;
    private int baseHealth;
    private String selectedDefense;
            
    public GameManager(){
       
    }
    
    public void startGame(){
        
        int delay = 17;
        gameTimer = new Timer(delay, new ActionListener() {
            private int frameCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO:
                frameCount++;
                System.out.println("Ejecución " + frameCount);
                //TODO:
                // Aquí va el código que quieres ejecutar 60 veces por segundo
                // por ejemplo, actualizar un juego o una interfaz gráfica
            }
        });
        
        gameTimer.start();
    }
    
    public void pauseGame(){
        
        gameTimer.stop();
    }
    
    public void placeDefences(){
        
        
    }
    
    public void removeDenfences(){
        
        
    }
    
    public void generateWave(){
        
    }
    
    public void update(){
        
        
    }
    public void isThereSpace(){
        
        
    }
    
    public void verifyVictory(){
        
    }
    
    public void verifyLoss(){
        
        
    }
}
