package Defense;

import Entity.Entity;
import java.util.ArrayList;

public class Defense extends Entity implements Runnable {

    protected ArrayList<DefenseType> types;
    
    public Defense() {
       this.types = new ArrayList<>();
       this.types.add(DefenseType.BLOCKS);
    }
    
    public Defense(String name, int healthPoints, int showUpLevel, int cost) {
        this.entityName = name;
        this.healthPoints = healthPoints;
        this.showUpLevel = showUpLevel;
        this.cost = cost;
        this.types = new ArrayList<>();
    }
    
    public Defense(DefenseType type, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.types = new ArrayList<>();
        this.types.add(type);
    }
    
    public Defense(ArrayList<DefenseType> types, String name, int healthPoints, int showUpLevel, int cost) {
        this(name, healthPoints, showUpLevel, cost);
        this.types = new ArrayList<>(types);
    }
    
    public ArrayList<DefenseType> getTypes() {
        return types;
    }
    
    public void setTypes(ArrayList<DefenseType> types) {
        this.types = types;
    }
    
    public boolean hasType(DefenseType type) {
        return types.contains(type);
    }
    
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getShowUpLevel() {
        return showUpLevel;
    }

    public void setShowUpLevel(int showUpLevel) {
        this.showUpLevel = showUpLevel;
    }
    
    // ==================== COMBAT CAPABILITIES IMPLEMENTATION ====================
    
    @Override
    public boolean isFlying() {
        return types.contains(DefenseType.FLYING);
    }
    
    @Override
    public boolean isExplosive() {
        return types.contains(DefenseType.EXPLOSIVE);
    }
    
    @Override
    public boolean isHealer() {
        return types.contains(DefenseType.HEALER);
    }
    
    @Override
    public int getAttackRange() {
        // Priority order: EXPLOSIVE > CONTACT > FLYING+MEDIUMRANGE > FLYING > MEDIUMRANGE > HEALER
        
        // EXPLOSIVE always has contact range (1) for triggering
        if (types.contains(DefenseType.EXPLOSIVE)) {
            return 1;
        }
        
        // CONTACT has range 1
        if (types.contains(DefenseType.CONTACT)) {
            return 1;
        }
        
        // FLYING + MEDIUMRANGE combination = 5x5 (radius 2)
        if (types.contains(DefenseType.FLYING) && types.contains(DefenseType.MEDIUMRANGE)) {
            return 2;
        }
        
        // FLYING alone = 5x5 (radius 2)
        if (types.contains(DefenseType.FLYING)) {
            return 2;
        }
        
        // MEDIUMRANGE = 7x7 (radius 3)
        if (types.contains(DefenseType.MEDIUMRANGE)) {
            return 3;
        }
        
        // HEALER = 7x7 (radius 3)
        if (types.contains(DefenseType.HEALER)) {
            return 3;
        }
        
        // BLOCKS and default
        return 0;
    }
    
    @Override
    public boolean hasMultipleAttacks() {
        return types.contains(DefenseType.MULTIPLEATTACK);
    }
    
    // ==================== THREAD METHODS ====================
    
    /**
     * Set the game manager for this defense
     */
    public void setGameManager(GameManager gm) {
        this.gameManager = gm;
    }
    
    /**
     * Main thread loop for defense behavior
     * Implements: "Las defensas fijan el objetivo que se ponga en su alcance"
     */
    @Override
    public void run() {
        while (running && healthPoints > 0) {
            try {
                if (gameManager == null || gameManager.isGamePaused()) {
                    Thread.sleep(100);
                    continue;
                }
                
                // BLOCKS type defenses don't attack
                if (!hasType(DefenseType.BLOCKS)) {
                    // Check if locked target is still valid
                    if (lockedTarget != null) {
                        if (lockedTarget.getHealthPoints() <= 0 || !isTargetInRange(lockedTarget)) {
                            // Target died or left range, release lock
                            lockedTarget = null;
                        }
                    }
                    
                    // If no locked target, find one
                    if (lockedTarget == null) {
                        lockedTarget = gameManager.findClosestZombieInRange(this);
                    }
                    
                    // Attack the locked target (or search for targets if no lock)
                    performAttack();
                }
                
                // Wait before next attack
                Thread.sleep(ATTACK_DELAY);
                
            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Error in defense thread " + getDisplayName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Check if target is still in attack range
     */
    private boolean isTargetInRange(Entity target) {
        if (target == null || gameManager == null) return false;
        int distance = gameManager.calculateDistanceBetween(this, target);
        return distance <= this.getAttackRange();
    }
    
    /**
     * Get the currently locked target
     */
    public Entity getLockedTarget() {
        return lockedTarget;
    }
    
    /**
     * Perform attack logic (to be called by the thread)
     */
    private void performAttack() {
        if (gameManager == null) return;
        
        // Delegate to GameManager to handle the actual attack
        // This keeps combat logic centralized and synchronized
        gameManager.processDefenseAttackThreaded(this);
    }
    
    /**
     * Start the defense thread
     */
    public void startThread() {
        if (defenseThread == null || !defenseThread.isAlive()) {
            running = true;
            defenseThread = new Thread(this, "Defense-" + getDisplayName());
            defenseThread.setDaemon(true); // Allow JVM to exit even if thread is running
            defenseThread.start();
            System.out.println("▶ Started thread for " + getDisplayName());
        }
    }
    
    /**
     * Stop the defense thread
     */
    public void stopThread() {
        running = false;
        if (defenseThread != null) {
            defenseThread.interrupt();
        }
    }
    
    /**
     * Check if thread is running
     */
    public boolean isThreadRunning() {
        return running && defenseThread != null && defenseThread.isAlive();
    }
}
