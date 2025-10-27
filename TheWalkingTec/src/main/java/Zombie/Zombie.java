package Zombie;

import Entity.Entity;
import GameLogic.GameManager;

public class Zombie extends Entity {
    
    protected ZombieType type;
    
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
    
    public Zombie() {
        type = ZombieType.CONTACT;
        initializeMovement(1.5);
    }
    
    public Zombie(String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        if (type == null) { type = ZombieType.CONTACT; }
        initializeMovement(movementSpeed);
    }
    
    public Zombie(ZombieType type, String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.type = type;
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
    
    public ZombieType getType() { return type; }
    
    public void setType(ZombieType type) { this.type = type; }
    
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
     * Thread run method - handles zombie movement and behavior
     */
    @Override
    public void run() {
        while (isAlive && gameManager != null) {
            try {
                // Request next move from GameManager
                if (gameManager != null) {
                    gameManager.moveZombieTowardsLifeTree(this);
                }
                
                // Sleep to control update rate (60 FPS)
                Thread.sleep(16);
                
            } catch (InterruptedException e) {
                isAlive = false;
                break;
            }
        }
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
    
}
