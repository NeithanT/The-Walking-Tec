package Table;
import GameLogic.GameManager;
import Defense.Defense;
import Entity.Entity;
import Zombie.Zombie;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
public class GameBoard extends JPanel {

    private Image backgroundImage;
    private static final String IMAGE_PATH = "/assets/tablero.png";

    private ArrayList<PlacedDefense> defenses;
    private ArrayList<Object> zombies;
    
    // Locks para acceso thread-safe sin synchronized
    private final ReentrantLock defensesLock = new ReentrantLock();
    private final ReentrantLock zombiesLock = new ReentrantLock();

    private final int ROWS = 25;
    private final int COLUMNS = 25;
    private double cellWidth;
    private double cellHeight;

    private int rowPreview = -1;
    private int columnPreview = -1;
    private boolean showPreview = false;
    private String selectedDefenseString = null;
    private boolean sellMode = false;
    
    private GameManager gameManager;
    
    // Prevent duplicate dialog openings
    private long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_MS = 500;
    
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
        
        if (gameManager == null){
            return;
        }
        
        int column = pixelToCellColumn(event.getX());
        int row = pixelToCellRow(event.getY());
        
        // If in sell mode, try to sell the defense at this position
        if (sellMode) {
            gameManager.sellDefenseAt(row, column);
            return;
        }
        
        // If round is active and combat log exists, check for entity click to show stats
        if (gameManager.isRoundActive() && gameManager.getCombatLog() != null) {
            Entity clickedEntity = getEntityAt(row, column);
            if (clickedEntity != null) {
                // Debounce: prevent duplicate clicks within 500ms
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < CLICK_DEBOUNCE_MS) {
                    return; // Ignore duplicate click
                }
                lastClickTime = currentTime;
                
                // Show entity stats dialog
                javax.swing.JFrame parentFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
                if (parentFrame != null) {
                    Table.EntityStatsDialog.showStats(parentFrame, clickedEntity, gameManager.getCombatLog());
                }
                return; // Don't place defense if clicking on entity
            }
        }
        
        // Otherwise, place a new defense if one is selected
        if (selectedDefenseString != null) {
            gameManager.placeDefences(row, column);
        }
    }
    
    private void handleMouseMove(MouseEvent event){
        
        if (sellMode) {
            // In sell mode, don't show preview
            showPreview = false;
            repaint();
            return;
        }
        
        if (selectedDefenseString == null){
            showPreview = false;
            return;
        }
        columnPreview = pixelToCellColumn(event.getX());
        rowPreview = pixelToCellRow(event.getY());
        showPreview = true;
        repaint();
    }
    
    private Entity getEntityAt(int row, int column) {
        // Check defenses first
        for (PlacedDefense placedDefense : defenses) {
            if (placedDefense != null) {
                Defense defense = placedDefense.definition;
                if (defense != null && defense.getCurrentRow() == row && defense.getCurrentColumn() == column) {
                    return defense;
                }
            }
        }
        
        // Then check zombies
        for (Object obj : zombies) {
            if (obj instanceof Zombie) {
                Zombie zombie = (Zombie) obj;
                if (zombie != null && zombie.getCurrentRow() == row && zombie.getCurrentColumn() == column) {
                    return zombie;
                }
            }
        }
        
        return null;
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
        defensesLock.lock();
        try {
            defensesCopy = new ArrayList<>(defenses);
        } finally {
            defensesLock.unlock();
        }
        
        for (PlacedDefense defense : defensesCopy){
            if (defense.image != null){
                
                int x = (int)(defense.column * cellWidth);
                int y = (int)(defense.row * cellHeight);
                int w = (int) cellWidth;
                int h = (int) cellHeight;
                g.drawImage(defense.image, x, y, w, h, this);
                
                // If in sell mode, draw a red overlay to indicate towers can be sold
                if (sellMode) {
                    g.setColor(new Color(255, 0, 0, 80)); // Semi-transparent red
                    g.fillRect(x, y, w, h);
                    g.setColor(new Color(255, 0, 0, 200)); // Brighter red border
                    g.drawRect(x, y, w, h);
                }
            }
        }
       
        // Draw zombies at their pixel positions
        // Create a copy to avoid ConcurrentModificationException
        ArrayList<Object> zombiesCopy;
        zombiesLock.lock();
        try {
            zombiesCopy = new ArrayList<>(zombies);
        } finally {
            zombiesLock.unlock();
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
        defensesLock.lock();
        try {
            defenses.add(defense);
        } finally {
            defensesLock.unlock();
        }
        repaint();
    }

    public void addZombie(Object zombie){
        zombiesLock.lock();
        try {
            zombies.add(zombie);
        } finally {
            zombiesLock.unlock();
        }
        repaint();

    }

    public void deleteDefense(PlacedDefense defense){
        defensesLock.lock();
        try {
            defenses.remove(defense);
        } finally {
            defensesLock.unlock();
        }
        repaint();
    }
    
    public void removePlacedDefense(int row, int col) {
        defensesLock.lock();
        try {
            defenses.removeIf(pd -> pd.row == row && pd.column == col);
        } finally {
            defensesLock.unlock();
        }
        repaint();
    }

    public void deleteZombie(Object zombie){
        zombiesLock.lock();
        try {
            zombies.remove(zombie);
        } finally {
            zombiesLock.unlock();
        }
        repaint();
    }

    public void clearZombies(){
        zombiesLock.lock();
        try {
            zombies.clear();
        } finally {
            zombiesLock.unlock();
        }
        repaint();
    }
    
    public void clearDefenses(){
        defensesLock.lock();
        try {
            defenses.clear();
        } finally {
            defensesLock.unlock();
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
        this.sellMode = false;
        repaint();
    }
    
    public void setSellMode(boolean enabled) {
        this.sellMode = enabled;
        if (enabled) {
            // Clear defense selection when entering sell mode
            this.selectedDefenseString = null;
            this.showPreview = false;
        }
        repaint();
    }
    
    public boolean isSellMode() {
        return sellMode;
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
        defensesLock.lock();
        try {
            for (PlacedDefense defense : defenses) {
                if (defense.row == row && defense.column == column) {
                    return defense;
                }
            }
        } finally {
            defensesLock.unlock();
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