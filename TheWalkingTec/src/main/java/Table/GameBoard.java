package Table;
import GameLogic.GameManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
public class GameBoard extends JPanel {

    private Image backgroundImage;
    private static final String IMAGE_PATH = "/assets/tablero.png";

    private ArrayList<Object> defenses;
    private ArrayList<Object> zombies;

    private final int ROWS = 25;
    private final int COLUMNS = 25;
    private double cellWidth;
    private double cellHeight;

    private int rowPreview = -1;
    private int columnPreview = -1;
    private boolean showPreview = false;
    private String selectedDefenseString = null;
    
    private GameManager gameManager;
    
    public GameBoard(){

        this.setLayout(null);

        defenses = new ArrayList<>();
        zombies = new ArrayList<>();

        try {          
            backgroundImage = ImageIO.read(getClass().getResource(IMAGE_PATH));
        } 
        catch (IOException | NullPointerException e) {
            System.err.println("¡Error! No se pudo cargar la imagen de fondo: " + IMAGE_PATH);
        }
        
        setupMouseListeners();
        
    }
        
    private void setupMouseListeners(){
        
        MouseAdapter mouseHandler = new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent event){
                handleClick(event);
            }
            
            @Override
            public void mouseMoved(MouseEvent event){
                handleMouseMove(event);   
            }
            
            @Override
            public void mouseExited(MouseEvent event){
                showPreview = false;
                repaint();
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }
    
    private void handleClick (MouseEvent event){
        
        if (gameManager == null || selectedDefenseString == null){
            return;
        }
        int column = pixelToCellColumn(event.getX());
        int row = pixelToCellRow(event.getY());
        
        gameManager.placeDefences(row, column);
    }
    
    private void handleMouseMove(MouseEvent event){
        
        if (selectedDefenseString == null){
            showPreview = false;
            return;
        }
        columnPreview = pixelToCellColumn(event.getX());
        rowPreview = pixelToCellRow(event.getY());
        showPreview = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 

        cellWidth = (double) getWidth() / COLUMNS;
        cellHeight = (double) getHeight() / ROWS;

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } 
        else 
        {     
            g.setColor(java.awt.Color.BLACK); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        if (showPreview && rowPreview != -1){
            drawPreview(g);
        }
        
//        if ()
        
        //TODO: DIBUJAR DEFENSAS Y ZOMBIES
    }

    private void drawPreview(Graphics g){
        
        int x = (int)(columnPreview * cellWidth);
        int y = (int)(rowPreview * cellHeight);
        int w = (int)cellWidth;
        int h = (int)cellHeight;
        
        boolean valid = gameManager != null && gameManager.isValidPlacement(rowPreview, columnPreview);
        if (valid){
            g.setColor(new Color(0, 255, 0, 100));
        }
        else{ 
            g.setColor(new Color(255, 0, 0, 100));
        }
        g.fillRect(x, y, w, h);
        g.setColor(valid ? Color.GREEN : Color.RED);
        g.drawRect(x, y, w, h);
    }
    
    
    public int pixelToCellColumn(int x){ 
        int column = (int)(x / cellWidth);
        return (column >= COLUMNS) ? COLUMNS - 1 : column;
    }

    public int pixelToCellRow(int y){
        int row = (int) (y / cellHeight);
        return (row >= ROWS) ? ROWS - 1 : row;    
    }

    public int cellToPixelX (int column){

         return (int) (column * cellWidth + cellWidth / 2.0);   
    }

    public int cellToPixelY (int row){

        return (int)(row * cellHeight + cellHeight / 2.0);
    }

    public void addDefense(Object defense){

        defenses.add(defense);
        repaint();
    }

    public void addZombie(Object zombie){

        zombies.add(zombie);

    }

    public void deleteDefense(Object defense){

        defenses.remove(defense);
        repaint();
    }

    public void deleteZombie(Object zombie){

        zombies.remove(zombie);
    }

    public void clearZombies(){
        zombies.clear();
        repaint();
    }
        
    public void setSelectedDefense(String type){

        this.selectedDefenseString = type;
        this.showPreview = true;
    }    
        
    
    public void clearSelectedDefense (){
        
        this.selectedDefenseString = null;
        this.showPreview = false;
        this.rowPreview = -1;
        this.columnPreview = -1;
        repaint();
    }
        
    public void setGameManger(GameManager manager){
        
        this.gameManager = manager;
    }    
    
    public ArrayList<Object> getDefenses() {
        return defenses;
    }
    public ArrayList<Object> getZombies() {
        return zombies;
    }
}