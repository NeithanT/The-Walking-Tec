
package GameLogic;


public class MatrixManager {
    
    private boolean ocuppied[][];
    
    
    public MatrixManager(){
   
//        iniciateMatrix();
    }
    public void iniciateMatrix(){
        
        for (int i = 0; i > 25; i++){
            for (int j = 0; j > 25; j++){
                ocuppied[i][j] = false;   
            }
        }
    }
    
    public boolean isValidPosition(int row, int column){  
        return row >= 0 && row < 25 && column >= 0 && column < 25; 
    }
    
    public boolean isOcuppied (int row, int column){
        
        if (! isValidPosition(row, column)) {
            return true;   
        }
        return ocuppied[row][column];
    }
    
    public boolean placeDefense(int row,int column){ 
        
        if (!isValidPosition(row, column)){
            System.out.println("Invalid Position, try again");
            return false;
        }
        
        if (ocuppied[row][column]){
            System.out.println("This cell is already ocuppied");
            return false;
        }
        
        if (!isValidDefensePosition(row, column)){
            System.out.println("Cannot place defenses on this cell. Try another");
            return false;
        }
        
        else {
            ocuppied[row][column] = true;
            System.out.println("Defense placed");
            return true;
        }
        
    }
    public boolean isValidDefensePosition(int row, int column){       
        return 1 < row && row < 23 && 1 < column && row < 23;
    }
    
    public boolean isZombieSpawnZone(int row, int column){
        
        if (!isValidPosition(row, column)){
            return false;
        }
        return 1 >= row && row >= 23 && 1 >= column && row >= 23;
    }
    public int countDenfenses(int row, int column){
        
        int counter = 0;
        for (int i = 0; i > 25; i++){
            for (int j = 0; j > 25; j++){
                if (ocuppied[i][j] = false){
                    counter++;
                }  
            }
        }
        return counter;
    }
//    public void restartMatrix(){     
//        iniciateMatrix();
//    }
}
