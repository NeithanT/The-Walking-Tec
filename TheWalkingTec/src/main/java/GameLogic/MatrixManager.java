
package GameLogic;


public class MatrixManager {
    
    private boolean occupied[][];
    private GameManager gameManager;
    
    
    public MatrixManager(){
   
        occupied = new boolean[25][25];
        iniciateMatrix();
    }
    
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    public void iniciateMatrix(){
        
        for (int i = 0; i < 25; i++){
            for (int j = 0; j < 25; j++){
                occupied[i][j] = false;   
            }
        }
    }
    
    public boolean isValidPosition(int row, int column){  
        return row >= 0 && row < 25 && column >= 0 && column < 25; 
    }
    
    public boolean isOccupied (int row, int column){
        
        if (! isValidPosition(row, column)) {
            return true;   
        }
        return occupied[row][column];
    }
    
    public boolean placeDefense(int row,int column){ 
        
        if (!isValidPosition(row, column)){
            if (gameManager != null) {
                gameManager.getSidePanel().appendLog("Invalid Position, try again");
            }
            return false;
        }
        
        if (occupied[row][column]){
            if (gameManager != null) {
                gameManager.getSidePanel().appendLog("This cell is already ocuppied");
            }
            return false;
        }
        
        if (!isValidDefensePosition(row, column)){
            if (gameManager != null) {
                gameManager.getSidePanel().appendLog("Cannot place defenses on this cell. Try another");
            }
            return false;
        }
        
        else {
            occupied[row][column] = true;
            if (gameManager != null) {
                gameManager.getSidePanel().appendLog("Defense placed");
            }
            return true;
        }
        
    }
    public void free (int row, int column){
        if (isValidPosition(row, column)){
            occupied[row][column] = false;
        }
    }
    
    public boolean isValidDefensePosition(int row, int column){       
        return row >= 2 && row < 23 && column >= 2 && column < 23; 
    }
    
    public boolean isZombieSpawnZone(int row, int column){
        
        if (!isValidPosition(row, column)){
            return false;
        }
        
        return row == 0 || row == 1 || row == 23 || row == 24 || column == 0 || column == 1 || column == 23 || column == 24;
    }
    public int countDenfenses(){
        
        int counter = 0;
        for (int i = 0; i < 25; i++){
            for (int j = 0; j < 25; j++){
                if (occupied[i][j] == true){
                    counter++;
                }  
            }
        }
        return counter;
    }
    
    public void restartMatrix(){     
        iniciateMatrix();
    }
}
