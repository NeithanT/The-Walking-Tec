package Zombie;

import Entity.Entity;
import GameLogic.GameManager;
import java.util.ArrayList;

public class Zombie extends Entity implements Runnable {
    
    protected ArrayList<ZombieType> types;
    
    // Pixel position tracking (smooth movement)
    protected double pixelX;
    protected double pixelY;
    protected int targetRow;
    protected int targetColumn;
    
    // Movement control
    protected boolean isMoving;
    protected boolean isAlive;
    protected double movementSpeed; // cells per second
    protected GameManager gameManager;
    
    // Thread control
    private Thread zombieThread;
    private volatile boolean running = false;
    private static final long MOVEMENT_DELAY = 50; // 50ms = ~20 FPS
    private static final long ATTACK_DELAY = 1000; // 1 second between attacks
    
    public Zombie() {
        types = new ArrayList<>();
        types.add(ZombieType.CONTACT);
        initializeMovement(1.5);
    }
    
    public Zombie(String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        if (types == null) { types = new ArrayList<>(); }
        initializeMovement(movementSpeed);
    }
    
    public Zombie(ZombieType type, String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.types = new ArrayList<>();
        this.types.add(type);
        initializeMovement(movementSpeed);
    }
    
    public Zombie(ArrayList<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.types = new ArrayList<>(types);
        initializeMovement(movementSpeed);
    }
    
    private void initializeMovement(double movementSpeed) {
        this.isMoving = false;
        this.isAlive = true;
        this.movementSpeed = movementSpeed;
        this.currentRow = -1;
        this.currentColumn = -1;
        this.targetRow = -1;
        this.targetColumn = -1;
    }
    
    public ArrayList<ZombieType> getTypes() { 
        return types; 
    }
    
    public void setTypes(ArrayList<ZombieType> types) { 
        this.types = types; 
    }
    
    public boolean hasType(ZombieType type) {
        return types.contains(type);
    }
    
    // Pixel position getters and setters
    public double getPixelX() { return pixelX; }
    
    public void setPixelX(double x) { this.pixelX = x; }
    
    public double getPixelY() { return pixelY; }
    
    public void setPixelY(double y) { this.pixelY = y; }
    
    public int getTargetRow() { return targetRow; }
    
    public void setTargetRow(int row) { this.targetRow = row; }
    
    public int getTargetColumn() { return targetColumn; }
    
    public void setTargetColumn(int column) { this.targetColumn = column; }
    
    public void setAlive(boolean alive) { this.isAlive = alive; }
    
    public double getMovementSpeed() { return movementSpeed; }
    
    public void setMovementSpeed(double speed) { this.movementSpeed = speed; }
    
    public void setGameManager(GameManager gm) { this.gameManager = gm; }

    /**
     * Indicates whether the zombie is currently active in the game loop.
     */
    public boolean isEntityAlive() { return isAlive; }

    /**
     * Applies damage and toggles the internal alive flag if health reaches zero.
     * @param damage the amount of damage to absorb
     */
    public void applyDamage(int damage) {
        if (damage <= 0) {
            return;
        }
        healthPoints = Math.max(0, healthPoints - damage);
        if (healthPoints == 0) {
            isAlive = false;
        }
    }
    
    /**
     * Sets the initial spawn position for the zombie
     */
    public void setSpawnPosition(int row, int column, double pixelX, double pixelY) {
        this.currentRow = row;
        this.currentColumn = column;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.targetRow = row;
        this.targetColumn = column;
    }
    
    /**
     * Smoothly moves the zombie towards a target position
     */
    public void moveTowardsTarget(double targetPixelX, double targetPixelY, double deltaTime) {
        double dx = targetPixelX - pixelX;
        double dy = targetPixelY - pixelY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1.0) {
            // Reached target
            pixelX = targetPixelX;
            pixelY = targetPixelY;
            currentRow = targetRow;
            currentColumn = targetColumn;
            isMoving = false;
            return;
        }
        
        // Normalize direction and apply speed
        double moveDistance = movementSpeed * deltaTime * 50; // 50 pixels per cell approx
        if (moveDistance > distance) {
            moveDistance = distance;
        }
        
        pixelX += (dx / distance) * moveDistance;
        pixelY += (dy / distance) * moveDistance;
        isMoving = true;
    }
    
    // ==================== COMBAT CAPABILITIES IMPLEMENTATION ====================
    
    @Override
    public boolean isFlying() {
        return types.contains(ZombieType.FLYING);
    }
    
    @Override
    public boolean isExplosive() {
        return types.contains(ZombieType.EXPLOSIVE);
    }
    
    @Override
    public boolean isHealer() {
        return types.contains(ZombieType.HEALER);
    }
    
    @Override
    public int getAttackRange() {
        // Priority order: EXPLOSIVE > CONTACT > FLYING+MEDIUMRANGE > FLYING > MEDIUMRANGE > HEALER
        
        // EXPLOSIVE always has contact range (1) for triggering
        if (types.contains(ZombieType.EXPLOSIVE)) {
            return 1;
        }
        
        // CONTACT has range 1
        if (types.contains(ZombieType.CONTACT)) {
            return 1;
        }
        
        // FLYING + MEDIUMRANGE combination = 5x5 (radius 2)
        if (types.contains(ZombieType.FLYING) && types.contains(ZombieType.MEDIUMRANGE)) {
            return 2;
        }
        
        // FLYING alone = 5x5 (radius 2)
        if (types.contains(ZombieType.FLYING)) {
            return 2;
        }
        
        // MEDIUMRANGE = 7x7 (radius 3)
        if (types.contains(ZombieType.MEDIUMRANGE)) {
            return 3;
        }
        
        // HEALER = 7x7 (radius 3)
        if (types.contains(ZombieType.HEALER)) {
            return 3;
        }
        
        // Default (shouldn't reach here)
        return 1;
    }
    
    // ==================== THREAD METHODS ====================
    
    /**
     * Main thread loop for zombie behavior
     */
    @Override
    public void run() {
        long lastAttackTime = 0;
        
        while (running && isAlive && healthPoints > 0) {
            try {
                if (gameManager == null || gameManager.isGamePaused()) {
                    Thread.sleep(100);
                    continue;
                }
                
                // 1. Movement phase - move towards CLOSEST TARGET (defense or Life Tree)
                gameManager.moveZombieTowardsClosestTarget(this);
                
                // 2. Attack phase - check if can attack
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAttackTime >= ATTACK_DELAY) {
                    performAttack();
                    lastAttackTime = currentTime;
                }
                
                // Sleep to control movement speed
                Thread.sleep(MOVEMENT_DELAY);
                
            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Error in zombie thread " + getDisplayName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Cleanup when thread ends
        isAlive = false;
    }
    
    /**
     * Perform attack logic (to be called by the thread)
     */
    private void performAttack() {
        if (gameManager == null) return;
        
        // Delegate to GameManager to handle the actual attack
        // This keeps combat logic centralized and synchronized
        gameManager.processZombieAttackThreaded(this);
    }
    
    /**
     * Start the zombie thread
     */
    public void startThread() {
        if (zombieThread == null || !zombieThread.isAlive()) {
            running = true;
            zombieThread = new Thread(this, "Zombie-" + getDisplayName());
            zombieThread.setDaemon(true); // Allow JVM to exit even if thread is running
            zombieThread.start();
            System.out.println("▶ Started thread for " + getDisplayName());
        }
    }
    
    /**
     * Stop the zombie thread
     */
    public void stopThread() {
        running = false;
        if (zombieThread != null) {
            zombieThread.interrupt();
        }
    }
    
    /**
     * Check if thread is running
     */
    public boolean isThreadRunning() {
        return running && zombieThread != null && zombieThread.isAlive();
    }
    
}
