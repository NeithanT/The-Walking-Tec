package Zombie;

import Entity.Entity;
import GameLogic.GameManager;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class Zombie extends Entity {
    
    protected Set<ZombieType> types;
    
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
        types = new HashSet<>(Arrays.asList(ZombieType.CONTACT));
        initializeMovement(1.5);
    }
    
    public Zombie(String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        if (types == null) { types = new HashSet<>(); }
        initializeMovement(movementSpeed);
    }
    
    public Zombie(ZombieType type, String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.types = new HashSet<>(Arrays.asList(type));
        initializeMovement(movementSpeed);
    }
    
    public Zombie(Set<ZombieType> types, String name, int healthPoints, int showUpLevel, int cost, double movementSpeed) {
        this(name, healthPoints, showUpLevel, cost, movementSpeed);
        this.types = new HashSet<>(types);
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
    
    public Set<ZombieType> getTypes() { 
        return types; 
    }
    
    public void setTypes(Set<ZombieType> types) { 
        this.types = types; 
    }
    
    public boolean hasType(ZombieType type) {
        return types.contains(type);
    }
    
    // Backward compatibility - returns first type or CONTACT
    @Deprecated
    public ZombieType getType() { 
        return types.isEmpty() ? ZombieType.CONTACT : types.iterator().next();
    }
    
    @Deprecated
    public void setType(ZombieType type) { 
        this.types = new HashSet<>(Arrays.asList(type));
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
        // Explosive always has range 1 (contact)
        if (types.contains(ZombieType.EXPLOSIVE)) {
            return 1;
        }
        
        // For multiple types, return the longest range
        int maxRange = 0;
        for (ZombieType type : types) {
            int range = getRangeForType(type);
            if (range > maxRange) {
                maxRange = range;
            }
        }
        return maxRange > 0 ? maxRange : 1;
    }
    
    private int getRangeForType(ZombieType type) {
        switch (type) {
            case CONTACT:
                return 1; // Must be adjacent
            case FLYING:
                return 5; // 5x5 grid centered on zombie
            case MEDIUMRANGE:
                return 7; // 7x7 grid centered on zombie
            case EXPLOSIVE:
                return 1; // Same as contact - needs to be close to explode
            case HEALER:
                return 7; // 7x7 healing range
            default:
                return 1;
        }
    }
    
}

