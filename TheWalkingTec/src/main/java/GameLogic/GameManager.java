
package GameLogic;

import Configuration.ConfigManager;
import Defense.Defense;
import Defense.DefenseType;
import Table.GameBoard;
import Table.PlacedDefense;
import java.awt.event.ActionEvent;
import Table.SidePanel;
import Zombie.Zombie;
import Zombie.ZombieAttacker;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieMediumRange;
import Zombie.ZombieType;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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
    private int coinsThisLevel;
    private Defense lifeTree;
    private PlacedDefense lifeTreePlaced;
    private int lifeTreeRow = -1;
    private int lifeTreeColumn = -1;
    
    private final ConfigManager configMg;
    private final Random rnd = new Random();

    private int defenseCostLimit;
    private int defenseCostUsed;
    private int zombiesRemaining;
    private boolean roundActive;
    private boolean waveGenerated;
    
    private ArrayList<Zombie> waveZombies;
    private ArrayList<Defense> waveDefense;
    
    public GameManager(GameBoard board, SidePanel sidePanel){
        
        this.board = board;
        this.sidePanel = sidePanel;
        this.matrixManager = new MatrixManager();
        this.configMg = new ConfigManager();
        this.isPaused = true;
        this.level = 1;
        this.baseHealth = 100;
        this.selectedDefense = null;
        this.coinsThisLevel = coinsForLevel(level);
        this.defenseCostLimit = coinsThisLevel;
        this.defenseCostUsed = 0;
        this.roundActive = false;
        this.waveGenerated = false;
        this.waveZombies = new ArrayList<>();
        this.waveDefense = new ArrayList<>();
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
        
        if (!roundActive) {
            startRound();
        }

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
        stopZombieThreads();
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

        if (!canAffordDefense(selectedDefense)){
            System.out.println("Not enough defense capacity for this placement");
            return false;
        }
        if (isLifeTree && lifeTreePlaced != null){
            System.out.println("Life Tree already exists");
            return false;
        }
        
        if (!matrixManager.placeDefense(row, column)){
            return false;
        }
        
        Defense placedDefinition = selectedDefense;
        Image img = loadAndScale(placedDefinition.getImagePath());
        PlacedDefense placed = new PlacedDefense(placedDefinition, row, column, img);
        board.addDefense(placed);

        if (waveDefense != null && placedDefinition != null){
            waveDefense.add(placedDefinition);
        }
        
        
        if (isLifeTree){
            lifeTree = placedDefinition;
            lifeTreePlaced = placed;
            lifeTreeRow = row;
            lifeTreeColumn = column;
        }

        defenseCostUsed += Math.max(1, placedDefinition.getCost());

        System.out.println("Defensa [" + placedDefinition.getEntityName() + "] colocada en [" + row + "][" + column + "]"); 
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

        PlacedDefense target = board.getDefenseAt(row, column);
        if (target == null){
            System.out.println("No defense on selected cell");
            return false;
        }

        matrixManager.free(row, column);
        board.removeDefenseAt(row, column);
        if (waveDefense != null && target.definition != null){
            waveDefense.remove(target.definition);
        }
        defenseCostUsed = Math.max(0, defenseCostUsed - Math.max(1, target.definition.getCost()));

        if (target == lifeTreePlaced){
            lifeTreePlaced = null;
            lifeTree = null;
            lifeTreeRow = -1;
            lifeTreeColumn = -1;
        }

        board.repaint();
        return true;
    }
    
    public void generateWave(){
        if (waveGenerated) {
            return;
        }

        List<Zombie> pool = configMg.getZombies();
        if (pool == null || pool.isEmpty()) {
            System.out.println("No zombies configured. Cannot generate wave.");
            waveGenerated = true;
            return;
        }
        
        ArrayList<Zombie> availableZombies = new ArrayList<>();
        for (Zombie zombie : pool){
            if (zombie != null && zombie.getShowUpLevel() <= level){
                availableZombies.add(zombie);
            }
        }

        if (availableZombies.isEmpty()){
            for (Zombie zombie : pool){
                if (zombie != null){
                    availableZombies.add(zombie);
                }
            }
        }

        if (availableZombies.isEmpty()){
            System.out.println("No valid zombies found for the current round.");
            waveGenerated = true;
            return;
        }

        if (waveZombies == null){
            waveZombies = new ArrayList<>();
        }
        waveZombies.clear();

        int budget = coinsThisLevel;
        int attemptsWithoutFit = 0;
        int spawned = 0;
        int maxAttempts = Math.max(availableZombies.size() * 3, 15);
        zombiesRemaining = 0;
        if (board != null){
            board.clearZombies();
        }

        while (budget > 0 && attemptsWithoutFit < maxAttempts){
            Zombie prototype = availableZombies.get(rnd.nextInt(availableZombies.size()));
            int cost = Math.max(1, prototype.getCost());
            if (cost > budget){
                attemptsWithoutFit++;
                continue;
            }

            Zombie spawnedZombie = cloneZombie(prototype);
            if (spawnedZombie == null){
                attemptsWithoutFit++;
                continue;
            }

            // Initialize zombie with GameManager reference
            spawnedZombie.setGameManager(this);
            spawnedZombie.setAlive(true);
            
            spawned++;
            budget -= cost;
            attemptsWithoutFit = 0;
            zombiesRemaining++;
            waveZombies.add(spawnedZombie);
            if (board != null){
                board.addZombie(spawnedZombie);
            }
            System.out.println("Spawn zombie: " + (spawnedZombie.getEntityName() != null ? spawnedZombie.getEntityName() : "Zombie") + " (#" + spawned + ")");
        }

        zombiesRemaining = waveZombies.size();

        if (spawned == 0){
            System.out.println("No zombies could be spawned with the current round budget");
        } else {
            System.out.println("Wave level [" + level + "] generated with cost " + (coinsThisLevel - budget) + "/" + coinsThisLevel);
        }

        waveGenerated = true;
        
        // Start zombie threads after wave generation
        startZombieThreads();
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
    
    /**
     * Moves a zombie smoothly towards the lifeTree position
     * This method calculates the next move for a zombie using pathfinding
     * @param zombie The zombie to move
     */
    public synchronized void moveZombieTowardsLifeTree(Zombie zombie) {
        if (zombie == null || !zombie.isAlive() || isPaused) {
            return;
        }
        
        // Check if lifeTree exists
        if (lifeTreeRow < 0 || lifeTreeColumn < 0) {
            return;
        }
        
        // Initialize zombie position if not set
        if (zombie.getCurrentRow() < 0 || zombie.getCurrentColumn() < 0) {
            spawnZombieAtEdge(zombie);
        }
        
        // Calculate next target cell if zombie reached current target
        int currentRow = zombie.getCurrentRow();
        int currentColumn = zombie.getCurrentColumn();
        int targetRow = zombie.getTargetRow();
        int targetColumn = zombie.getTargetColumn();
        
        // Check if zombie needs a new target cell
        boolean needsNewTarget = (targetRow == currentRow && targetColumn == currentColumn) ||
                                  (targetRow < 0 || targetColumn < 0);
        
        if (needsNewTarget) {
            // Calculate next cell towards lifeTree
            int[] nextCell = calculateNextCellTowardsLifeTree(currentRow, currentColumn);
            zombie.setTargetRow(nextCell[0]);
            zombie.setTargetColumn(nextCell[1]);
        }
        
        // Calculate target pixel position
        double targetPixelX = board.cellToPixelX(zombie.getTargetColumn());
        double targetPixelY = board.cellToPixelY(zombie.getTargetRow());
        
        // Move zombie smoothly towards target
        double deltaTime = 0.016; // Approximately 60 FPS
        zombie.moveTowardsTarget(targetPixelX, targetPixelY, deltaTime);
        
        // Check if zombie reached lifeTree
        if (currentRow == lifeTreeRow && currentColumn == lifeTreeColumn) {
            zombieReachedLifeTree(zombie);
        }
    }
    
    /**
     * Spawns a zombie at a random edge position
     */
    private void spawnZombieAtEdge(Zombie zombie) {
        int row, column;
        
        // Choose random edge (0=top, 1=right, 2=bottom, 3=left)
        int edge = rnd.nextInt(4);
        
        switch (edge) {
            case 0: // Top edge
                row = rnd.nextInt(2); // 0 or 1
                column = rnd.nextInt(25);
                break;
            case 1: // Right edge
                row = rnd.nextInt(25);
                column = 23 + rnd.nextInt(2); // 23 or 24
                break;
            case 2: // Bottom edge
                row = 23 + rnd.nextInt(2); // 23 or 24
                column = rnd.nextInt(25);
                break;
            default: // Left edge
                row = rnd.nextInt(25);
                column = rnd.nextInt(2); // 0 or 1
                break;
        }
        
        double pixelX = board.cellToPixelX(column);
        double pixelY = board.cellToPixelY(row);
        
        zombie.setSpawnPosition(row, column, pixelX, pixelY);
    }
    
    /**
     * Calculates the next cell to move towards the lifeTree using simple pathfinding
     * @param currentRow Current row position
     * @param currentColumn Current column position
     * @return Array with [nextRow, nextColumn]
     */
    private int[] calculateNextCellTowardsLifeTree(int currentRow, int currentColumn) {
        int targetRow = lifeTreeRow;
        int targetColumn = lifeTreeColumn;
        
        // Calculate direction to lifeTree
        int rowDiff = targetRow - currentRow;
        int colDiff = targetColumn - currentColumn;
        
        // If already at lifeTree
        if (rowDiff == 0 && colDiff == 0) {
            return new int[]{currentRow, currentColumn};
        }
        
        // Determine movement priorities based on distance
        int nextRow = currentRow;
        int nextColumn = currentColumn;
        
        // Try to move in the direction with larger distance first
        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            // Prioritize vertical movement
            if (rowDiff > 0) {
                nextRow = currentRow + 1;
            } else if (rowDiff < 0) {
                nextRow = currentRow - 1;
            }
            
            // If vertical movement is blocked, try horizontal
            if (!isValidZombieMove(nextRow, nextColumn)) {
                nextRow = currentRow;
                if (colDiff > 0) {
                    nextColumn = currentColumn + 1;
                } else if (colDiff < 0) {
                    nextColumn = currentColumn - 1;
                }
            }
        } else {
            // Prioritize horizontal movement
            if (colDiff > 0) {
                nextColumn = currentColumn + 1;
            } else if (colDiff < 0) {
                nextColumn = currentColumn - 1;
            }
            
            // If horizontal movement is blocked, try vertical
            if (!isValidZombieMove(nextRow, nextColumn)) {
                nextColumn = currentColumn;
                if (rowDiff > 0) {
                    nextRow = currentRow + 1;
                } else if (rowDiff < 0) {
                    nextRow = currentRow - 1;
                }
            }
        }
        
        // If still blocked, try diagonal movement
        if (!isValidZombieMove(nextRow, nextColumn)) {
            nextRow = currentRow + (rowDiff > 0 ? 1 : rowDiff < 0 ? -1 : 0);
            nextColumn = currentColumn + (colDiff > 0 ? 1 : colDiff < 0 ? -1 : 0);
        }
        
        // Final validation - if still invalid, stay in place
        if (!isValidZombieMove(nextRow, nextColumn)) {
            nextRow = currentRow;
            nextColumn = currentColumn;
        }
        
        return new int[]{nextRow, nextColumn};
    }
    
    /**
     * Checks if a zombie can move to a specific cell
     */
    private boolean isValidZombieMove(int row, int column) {
        // Check if within bounds
        if (!matrixManager.isValidPosition(row, column)) {
            return false;
        }
        
        // Zombies can move through spawn zones and playable area
        // They can also move through cells with defenses (to attack them)
        return true;
    }
    
    /**
     * Handles when a zombie reaches the lifeTree
     */
    private void zombieReachedLifeTree(Zombie zombie) {
        if (zombie != null) {
            // Deal damage to base
            baseHealth -= zombie.getHealthPoints() / 10; // Damage based on zombie health
            
            // Remove zombie
            zombie.setAlive(false);
            registerZombieDefeat(zombie);
            
            System.out.println("Zombie reached the Life Tree! Base health: " + baseHealth);
        }
    }
    
    /**
     * Starts all zombie threads for the current wave
     */
    public void startZombieThreads() {
        if (waveZombies != null) {
            for (Zombie zombie : waveZombies) {
                if (zombie != null && !zombie.isAlive()) {
                    zombie.setAlive(true);
                    zombie.setGameManager(this);
                    zombie.start();
                }
            }
        }
    }
    
    /**
     * Stops all zombie threads
     */
    public void stopZombieThreads() {
        if (waveZombies != null) {
            for (Zombie zombie : waveZombies) {
                if (zombie != null) {
                    zombie.setAlive(false);
                }
            }
        }
    }
    
    private int coinsForLevel (int lvl){
        return 25 + 5 * (lvl - 1);
    }
    
    public boolean isValidPlacement(int row, int column){
        
        return !matrixManager.isOccupied(row, column) && matrixManager.isValidDefensePosition(row, column);
    }
    
    public boolean isThereSpaceLeft (int totalSpace){
        return defenseCostUsed < totalSpace;
    }
    
    public void verifyVictory(){
        if (!roundActive) {
            return;
        }

        if (zombiesRemaining <= 0 && board.getZombies().isEmpty() && waveGenerated){
            System.out.println("Mission Completed!");
            advanceRound();
        }
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
    
    public int getDefenseCostLimit() {
        return defenseCostLimit;
    }
    
    public int getDefenseCostUsed() {
        return defenseCostUsed;
    }

    public void registerZombieDefeat(Zombie zombie){
        if (zombie != null){
            board.deleteZombie(zombie);
        }
        zombiesRemaining = Math.max(0, zombiesRemaining - 1);
    }

    private boolean canAffordDefense(Defense defense){
        if (defense == null){
            return false;
        }
        int cost = Math.max(1, defense.getCost());
        return defenseCostUsed + cost <= defenseCostLimit;
    }

    private void startRound(){
        waveGenerated = false;
        roundActive = true;
        coinsThisLevel = coinsForLevel(level);
        defenseCostLimit = coinsThisLevel;
        waveZombies.clear();
        syncWaveDefenseWithBoard();
        generateWave();
    }

    private void advanceRound(){
        roundActive = false;
        waveGenerated = false;
        level++;
        coinsThisLevel = coinsForLevel(level);
        defenseCostLimit = coinsThisLevel;
        zombiesRemaining = 0;
        board.clearZombies();
        waveZombies.clear();
        syncWaveDefenseWithBoard();
        System.out.println("Starting round " + level);
        generateWave();
        roundActive = true;
    }

    private void syncWaveDefenseWithBoard(){
        if (waveDefense == null){
            waveDefense = new ArrayList<>();
        }

        waveDefense.clear();
        if (board != null){
            for (PlacedDefense placedDefense : board.getDefenses()){
                if (placedDefense != null && placedDefense.definition != null){
                    waveDefense.add(placedDefense.definition);
                }
            }
        }
    }

    private Zombie cloneZombie(Zombie source){
        if (source == null){
            return null;
        }

        Zombie clone;
        if (source instanceof ZombieExplosive explosive){
            clone = new ZombieExplosive(explosive.getEntityName(), explosive.getHealthPoints(), explosive.getShowUpLevel(), explosive.getCost(), explosive.getRange(), explosive.getMovementSpeed());
        } else if (source instanceof ZombieFlying flying){
            clone = new ZombieFlying(flying.getEntityName(), flying.getHealthPoints(), flying.getShowUpLevel(), flying.getCost(), flying.getDamage(), flying.getRange(), flying.getMovementSpeed());
        } else if (source instanceof ZombieMediumRange medium){
            clone = new ZombieMediumRange(medium.getEntityName(), medium.getHealthPoints(), medium.getShowUpLevel(), medium.getCost(), medium.getDamage(), medium.getMovementSpeed());
        } else if (source instanceof ZombieContact contact){
            clone = new ZombieContact(contact.getEntityName(), contact.getHealthPoints(), contact.getShowUpLevel(), contact.getCost(), contact.getDamage(), contact.getMovementSpeed());
        } else if (source instanceof ZombieHealer healer){
            clone = new ZombieHealer(healer.getEntityName(), healer.getHealthPoints(), healer.getShowUpLevel(), healer.getCost(), healer.getHealPower(), healer.getMovementSpeed());
        } else if (source instanceof ZombieAttacker attacker){
            clone = new ZombieAttacker(attacker.getEntityName(), attacker.getHealthPoints(), attacker.getShowUpLevel(), attacker.getCost(), attacker.getDamage(), attacker.getRange(), attacker.getMovementSpeed());
        } else {
            ZombieType type = source.getType();
            clone = new Zombie(type, source.getEntityName(), source.getHealthPoints(), source.getShowUpLevel(), source.getCost(), source.getMovementSpeed());
        }

        clone.setActions(source.getActions());
        clone.setImagePath(source.getImagePath());
        clone.setType(source.getType());
        return clone;
    }
    
}
