package Table;
import GameLogic.GameManager;
import Zombie.Zombie;
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

    private ArrayList<PlacedDefense> defenses;
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
        
        // Create a copy to avoid ConcurrentModificationException
        ArrayList<PlacedDefense> defensesCopy;
        synchronized(defenses) {
            defensesCopy = new ArrayList<>(defenses);
        }
        
        for (PlacedDefense defense : defensesCopy){
            if (defense.image != null){
                
                int x = (int)(defense.column * cellWidth);
                int y = (int)(defense.row * cellHeight);
                int w = (int) cellWidth;
                int h = (int) cellHeight;
                g.drawImage(defense.image, x, y, w, h, this);
            }
        }
       
        // Draw zombies at their pixel positions
        // Create a copy to avoid ConcurrentModificationException
        ArrayList<Object> zombiesCopy;
        synchronized(zombies) {
            zombiesCopy = new ArrayList<>(zombies);
        }
        
        for (Object obj : zombiesCopy) {
            if (obj instanceof Zombie) {
                Zombie zombie = (Zombie) obj;
                drawZombie(g, zombie);
            }
        }
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

    public void addDefense(PlacedDefense defense){
        synchronized(defenses) {
            defenses.add(defense);
        }
        repaint();
    }

    public void addZombie(Object zombie){
        synchronized(zombies) {
            zombies.add(zombie);
        }
        repaint();

    }

    public void deleteDefense(PlacedDefense defense){
        synchronized(defenses) {
            defenses.remove(defense);
        }
        repaint();
    }
    
    public void removePlacedDefense(int row, int col) {
        synchronized(defenses) {
            defenses.removeIf(pd -> pd.row == row && pd.column == col);
        }
        repaint();
    }

    public void deleteZombie(Object zombie){
        synchronized(zombies) {
            zombies.remove(zombie);
        }
        repaint();
    }

    public void clearZombies(){
        synchronized(zombies) {
            zombies.clear();
        }
        repaint();
    }
    
    public void clearDefenses(){
        synchronized(defenses) {
            defenses.clear();
        }
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
    
    public ArrayList<PlacedDefense> getDefenses() {
        return defenses;
    }
    public ArrayList<Object> getZombies() {
        return zombies;
    }

    public PlacedDefense getDefenseAt(int row, int column) {
        synchronized(defenses) {
            for (PlacedDefense defense : defenses) {
                if (defense.row == row && defense.column == column) {
                    return defense;
                }
            }
        }
        return null;
    }

    public PlacedDefense removeDefenseAt(int row, int column) {
        PlacedDefense defense = getDefenseAt(row, column);
        if (defense != null) {
            deleteDefense(defense);
        }
        return defense;
    }
    
    /**
     * Draws a zombie at its current pixel position
     */
    private void drawZombie(Graphics g, Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) {
            return;
        }
        
        // Get zombie's pixel position
        double pixelX = zombie.getPixelX();
        double pixelY = zombie.getPixelY();
        
        // Calculate cell size for zombie
        int w = (int) cellWidth;
        int h = (int) cellHeight;
        
        // Center the zombie in its position
        int x = (int) (pixelX - w / 2.0);
        int y = (int) (pixelY - h / 2.0);
        
        // Try to load and draw zombie image
        String imagePath = zombie.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image zombieImage = ImageIO.read(new java.io.File(imagePath));
                if (zombieImage != null) {
                    g.drawImage(zombieImage, x, y, w, h, this);
                    return;
                }
            } catch (Exception e) {
                // If image fails to load, fall through to draw default representation
            }
        }
        
        // Default: draw a colored circle to represent the zombie
        g.setColor(new Color(0, 150, 0)); // Dark green for zombie
        g.fillOval(x + w/4, y + h/4, w/2, h/2);
        g.setColor(Color.RED);
        g.drawOval(x + w/4, y + h/4, w/2, h/2);
        
        // Draw health bar
        int healthBarWidth = w;
        int healthBarHeight = 4;
        int healthBarX = x;
        int healthBarY = y - 8;
        
        // Background (red)
        g.setColor(Color.RED);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        
        // Health (green)
        g.setColor(Color.GREEN);
        int currentHealthWidth = (int) (healthBarWidth * (zombie.getHealthPoints() / 100.0));
        g.fillRect(healthBarX, healthBarY, Math.max(0, currentHealthWidth), healthBarHeight);
    }
}