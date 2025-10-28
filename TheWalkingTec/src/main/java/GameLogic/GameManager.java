package GameLogic;

import Configuration.ConfigManager;
import Defense.Defense;
import Defense.DefenseMultipleAttack;
import Defense.DefenseAttacker;
import Defense.DefenseHealer;
import Defense.DefenseType;
import Defense.DefenseContact;
import Defense.DefenseFlying;
import Defense.DefenseExplosive;
import Defense.DefenseMediumRange;
import Entity.Entity;
import Entity.EntityAttacker;
import Entity.EntityHealer;
import Table.GameBoard;
import Table.PlacedDefense;
import Table.SidePanel;
import Zombie.Zombie;
import Zombie.ZombieAttacker;
import Zombie.ZombieHealer;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.Timer;


public class GameManager {

    private static final String LIFE_TREE_NAME = "LIFE TREE";
    private static final int ZOMBIES_PER_SPAWN_BATCH = 5;
    private static final int SPAWN_DELAY_MS = 2000; // 2 segundos entre grupos
    
    // Locks para sincronización sin synchronized
    private final Lock combatLock = new ReentrantLock();
    private final Lock summaryLock = new ReentrantLock();

    private final GameBoard board;
    private final SidePanel sidePanel;
    private final MatrixManager matrixManager;
    private final ConfigManager configMg;
    private final Random rnd;
    private final WaveManager waveManager;
    private final ZombieMovementController movementController;
    private final ArrayList<Zombie> waveZombies;
    private final ArrayList<Defense> waveDefense;
    
    private CombatLog combatLog; // Combat statistics tracker
    
    private javax.swing.JFrame parentFrame; // Para mostrar diálogos

    private Timer gameTimer;
    private Timer zombieSpawnTimer;
    private Timer combatTimer;
    private int nextZombieIndexToSpawn;
    private int totalZombiesInWave; // Total zombies generated for this wave (doesn't change)
    private boolean isPaused;
    private int level;
    private int baseHealth;
    private Defense selectedDefense;
    private int coinsThisLevel;
    private int defenseCostLimit;
    private int defenseCostUsed;
    private int zombiesRemaining;
    private boolean roundActive;
    private boolean waveGenerated;
    private boolean victoryProcessed; // Flag to prevent multiple victory dialogs
    private boolean lossProcessed; // Flag to prevent multiple game over dialogs
    private boolean summaryShown; // Flag to prevent showing summary multiple times
    private Defense lifeTree;
    private PlacedDefense lifeTreePlaced;
    private int lifeTreeRow;
    private int lifeTreeColumn;
    private int lifeTreeInitialHealth; // Guardar salud inicial del Life Tree

    public GameManager(GameBoard board, SidePanel sidePanel) {

        this.board = board;
        this.sidePanel = sidePanel;
        this.parentFrame = null; // Se establecerá después
        this.matrixManager = new MatrixManager();
        this.matrixManager.setGameManager(this); // Set reference back
        this.configMg = new ConfigManager();
        this.rnd = new Random();
        this.waveManager = new WaveManager(this);
        this.movementController = new ZombieMovementController(this);
        this.waveZombies = new ArrayList<>();
        this.waveDefense = new ArrayList<>();

        this.isPaused = true;
        this.level = 1;
        this.baseHealth = 100;
        this.selectedDefense = null;
        this.coinsThisLevel = WaveManager.coinsForLevel(level);
        this.defenseCostLimit = coinsThisLevel;
        this.defenseCostUsed = 0;
        this.zombiesRemaining = 0;
        this.roundActive = false;
        this.waveGenerated = false;
        this.victoryProcessed = false;
        this.lossProcessed = false;
        this.summaryShown = false;
        this.nextZombieIndexToSpawn = 0;
        this.totalZombiesInWave = 0;
        this.lifeTree = null;
        this.lifeTreePlaced = null;
        this.lifeTreeRow = -1;
        this.lifeTreeColumn = -1;
        this.lifeTreeInitialHealth = 100; // Valor por defecto
    }
    
    public boolean startGame() {
        // Evitar que se inicie el juego múltiples veces
        if (gameTimer != null && gameTimer.isRunning()) {
            log("Game is already running!");
            return false;
        }

        if (lifeTreePlaced == null) {
            log("===========================================");
            log("  ERROR: You must place the Life Tree     ");
            log("         before starting the game!        ");
            log("===========================================");
            return false;
        }
        if (gameTimer == null) {
            int delay = 17;
            gameTimer = new Timer(delay, e -> update());
        }
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
        
        // Inicializar combat timer (procesa combate cada 1 segundo)
        // Recrear el timer si fue detenido para evitar problemas de estado
        if (combatTimer != null) {
            combatTimer.stop(); // Asegurar que está detenido
        }
        int combatDelay = 1000; // 1 segundo
        combatTimer = new Timer(combatDelay, e -> processCombat());
        combatTimer.start();
        log("Combat timer (re)created and started");
        
        isPaused = false;

        if (!roundActive) {
            startRound();
        }

        log("Juego iniciado");
        return true;
    }

    public void pauseGame() {

        isPaused = !isPaused;
        String message = isPaused ? "Juego Pausado" : "Juego reanudado";
        log(message);
    }

    public void stopGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (zombieSpawnTimer != null && zombieSpawnTimer.isRunning()) {
            zombieSpawnTimer.stop();
        }
        if (combatTimer != null && combatTimer.isRunning()) {
            combatTimer.stop();
        }
        isPaused = true;
        stopZombieThreads();
        stopDefenseThreads();
    }
    
    /**
     * Stop all defense threads
     */
    private void stopDefenseThreads() {
        System.out.println("⏹ Stopping all defense threads...");
        for (Defense defense : waveDefense) {
            if (defense != null) {
                defense.stopThread();
            }
        }
    }

    public void setSelectedDefense(Defense defenseName) {

        this.selectedDefense = defenseName;
        board.setSelectedDefense(defenseName != null ? defenseName.getEntityName() : null);
        log("Selected Defense: " + (defenseName != null ? defenseName.getEntityName() : "null"));
    }
    
    /**
     * Sells a defense at the given position, refunding its cost
     * If it's the Life Tree, it will be returned to the catalog
     */
    public boolean sellDefenseAt(int row, int column) {
        // No permitir vender defensas si la ronda está activa
        if (roundActive) {
            log("Cannot sell defenses while a round is in progress!");
            return false;
        }
        
        // Get the defense at this position
        PlacedDefense placed = board.getDefenseAt(row, column);
        if (placed == null || placed.definition == null) {
            log("No defense at this position to sell");
            return false;
        }
        
        Defense defense = placed.definition;
        String defenseName = defense.getEntityName();
        boolean isLifeTree = LIFE_TREE_NAME.equalsIgnoreCase(defenseName);
        
        // Remove from board visually
        board.removePlacedDefense(row, column);
        
        // Free the matrix position
        matrixManager.free(row, column);
        
        // Remove from combat list if it's not the Life Tree
        if (!isLifeTree) {
            waveDefense.remove(defense);
            
            // Refund the cost
            int refund = Math.max(1, defense.getCost());
            defenseCostUsed -= refund;
            if (defenseCostUsed < 0) {
                defenseCostUsed = 0;
            }
            log("Sold " + defenseName + " for " + refund + " coins");
        } else {
            // It's the Life Tree - return it to the catalog
            lifeTree = null;
            lifeTreePlaced = null;
            lifeTreeRow = -1;
            lifeTreeColumn = -1;
            
            // Restore base health to initial value or default
            baseHealth = 100;
            
            log("Life Tree returned to catalog");
            
            // Show Life Tree in catalog again
            if (sidePanel != null) {
                sidePanel.showLifeTreeInCatalog();
            }
        }
        
        // Update UI
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
        
        board.repaint();
        return true;
    }
    
    public boolean placeDefences(int row, int column) {
        // No permitir colocar defensas si la ronda está activa (jugando)
        if (roundActive) {
            log("Cannot place defenses while a round is in progress!");
            return false;
        }

        if (selectedDefense == null) {
            return false;
        }

        boolean isLifeTreeSelection = LIFE_TREE_NAME.equalsIgnoreCase(selectedDefense.getEntityName());

        // El Life Tree no consume recursos, así que no verificar capacidad
        if (!isLifeTreeSelection && !canAffordDefense(selectedDefense)) {
            log("Not enough defense capacity for this placement");
            return false;
        }

        if (isLifeTreeSelection && lifeTreePlaced != null) {
            log("Life Tree already exists");
            return false;
        }

        if (!matrixManager.placeDefense(row, column)) {
            return false;
        }

        // Crear una copia de la defensa seleccionada para esta colocación
        Defense placedDefinition = cloneDefense(selectedDefense);
        placedDefinition.setCurrentRow(row);
        placedDefinition.setCurrentColumn(column);
        
        // Apply level scaling to the defense
        if (!isLifeTreeSelection) {
            applyDefenseScaling(placedDefinition);
        }
        
        Image img = loadAndScale(placedDefinition.getImagePath());
        PlacedDefense placed = new PlacedDefense(placedDefinition, row, column, img);
        board.addDefense(placed);
        
        // Agregar a la lista de defensas activas para combate (excepto Life Tree)
        if (!isLifeTreeSelection) {
            waveDefense.add(placedDefinition);
            log("Defense added to combat list: " + placedDefinition.getEntityName() + " at (" + row + ", " + column + ")");
        }

        // El Life Tree no consume recursos
        if (!isLifeTreeSelection) {
            defenseCostUsed += Math.max(1, placedDefinition.getCost());
        }

        if (isLifeTreeSelection) {
            lifeTree = placedDefinition;
            lifeTreePlaced = placed;
            lifeTreeRow = row;
            lifeTreeColumn = column;
            baseHealth = lifeTree.getHealthPoints();
            lifeTreeInitialHealth = lifeTree.getHealthPoints(); // Guardar salud inicial
            log("Life Tree placed! Health: " + baseHealth);
            
            // Notificar al panel lateral para que oculte el Life Tree del catálogo
            if (sidePanel != null) {
                sidePanel.hideLifeTreeFromCatalog();
            }
            
            // Life Tree solo se coloca una vez, deseleccionarlo después
            selectedDefense = null;
            board.clearSelectedDefense();
            if (sidePanel != null) {
                sidePanel.deselectDefense();
            }
        } else {
            waveDefense.add(placedDefinition);
            // No deseleccionar la defensa para permitir colocación múltiple
            
            // Start defense thread if game is active
            if (roundActive && !isPaused) {
                placedDefinition.setGameManager(this);
                placedDefinition.startThread();
            }
        }

        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }

        return true;
    }
    
    public void update() {

        if (isPaused) {
            return;
        }

        board.repaint();
        verifyVictory();
        verifyLoss();
    }

    public void moveZombieTowardsLifeTree(Zombie zombie) {
        movementController.moveZombieTowardsLifeTree(zombie);
    }
    
    /**
     * Move zombie towards the CLOSEST target (defense or Life Tree)
     * This implements: "Los zombies buscan el objetivo más cercano y se desplazan hacia él"
     */
    public void moveZombieTowardsClosestTarget(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) {
            return;
        }
        
        // Find closest defense
        Defense closestDefense = findClosestDefense(zombie);
        
        // If there's a defense in range or on the path, move towards it
        if (closestDefense != null) {
            movementController.moveZombieTowardsTarget(zombie, 
                closestDefense.getCurrentRow(), 
                closestDefense.getCurrentColumn());
        } else {
            // No defenses, move towards Life Tree
            movementController.moveZombieTowardsLifeTree(zombie);
        }
    }
    
    /**
     * Find the closest defense to a zombie
     */
    private Defense findClosestDefense(Zombie zombie) {
        Defense closest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Defense defense : waveDefense) {
            if (defense != null && defense.getHealthPoints() > 0) {
                int distance = CombatRules.calculateDistance(zombie, defense);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = defense;
                }
            }
        }
        
        return closest;
    }
    
    /**
     * Find the closest zombie in range for a defense
     * Implements: "Las defensas fijan el objetivo que se ponga en su alcance"
     */
    public Zombie findClosestZombieInRange(Defense defense) {
        if (defense == null) return null;
        
        Zombie closest = null;
        int minDistance = Integer.MAX_VALUE;
        int defenseRange = defense.getAttackRange();
        
        for (Zombie zombie : waveZombies) {
            if (zombie != null && zombie.isAlive() && zombie.getHealthPoints() > 0) {
                int distance = CombatRules.calculateDistance(defense, zombie);
                if (distance <= defenseRange && distance < minDistance) {
                    minDistance = distance;
                    closest = zombie;
                }
            }
        }
        
        return closest;
    }
    
    /**
     * Calculate distance between two entities (helper method)
     */
    public int calculateDistanceBetween(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null) return Integer.MAX_VALUE;
        return CombatRules.calculateDistance(entity1, entity2);
    }

    void zombieReachedLifeTree(Zombie zombie) {
        if (zombie == null) {
            return;
        }

        if (lifeTree == null || lifeTree.getHealthPoints() <= 0) {
            zombie.setAlive(false);
            return;
        }

        int damage = Math.max(1, zombie.getHealthPoints() / 10);
        baseHealth -= damage;

        int newLifeTreeHealth = Math.max(0, lifeTree.getHealthPoints() - damage);
        lifeTree.setHealthPoints(newLifeTreeHealth);

        zombie.setAlive(false);
        
        // Log zombie death (killed by Life Tree)
        if (combatLog != null) {
            combatLog.logDeath(zombie, lifeTree);
        }
        
        registerZombieDefeat(zombie);

        log("Zombie attacked the Life Tree! Damage: " + damage
                + " | Life Tree health: " + lifeTree.getHealthPoints()
                + " | Base health: " + baseHealth);

        if (lifeTree.getHealthPoints() <= 0) {
            log("The Life Tree has been destroyed!");
            destroyLifeTree();
        }
        
        // Update UI after zombie attack on Life Tree
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }

        board.repaint();
    }

    public void startZombieThreads() {
        // Resetear el índice de spawn
        nextZombieIndexToSpawn = 0;
        
        log("Starting zombie spawn system. Total zombies: " + waveZombies.size());
        
        // Si ya hay un timer de spawn activo, detenerlo
        if (zombieSpawnTimer != null && zombieSpawnTimer.isRunning()) {
            zombieSpawnTimer.stop();
        }
        
        // Spawnear el primer grupo inmediatamente
        spawnNextZombieBatch();
        
        // Crear un timer para spawnear los grupos restantes
        if (nextZombieIndexToSpawn < waveZombies.size()) {
            zombieSpawnTimer = new Timer(SPAWN_DELAY_MS, e -> {
                if (!isPaused && roundActive) {
                    spawnNextZombieBatch();
                    
                    // Si ya spawneamos todos, detener el timer
                    if (nextZombieIndexToSpawn >= waveZombies.size()) {
                        ((Timer)e.getSource()).stop();
                        log("All zombies spawned! Timer stopped.");
                    }
                }
            });
            zombieSpawnTimer.setRepeats(true); // Asegurar que se repita
            zombieSpawnTimer.start();
            log("Zombie spawn timer started with " + SPAWN_DELAY_MS + "ms delay");
        } else {
            log("All zombies spawned in first batch");
        }
    }
    
    private void spawnNextZombieBatch() {
        // Check if there are zombies to spawn
        if (waveZombies.isEmpty()) {
            log("WARNING: waveZombies is empty, cannot spawn");
            if (zombieSpawnTimer != null && zombieSpawnTimer.isRunning()) {
                zombieSpawnTimer.stop();
            }
            return;
        }
        
        if (nextZombieIndexToSpawn >= waveZombies.size()) {
            log("WARNING: All zombies already spawned (" + nextZombieIndexToSpawn + "/" + waveZombies.size() + "), stopping timer");
            if (zombieSpawnTimer != null && zombieSpawnTimer.isRunning()) {
                zombieSpawnTimer.stop();
            }
            return;
        }
        
        int endIndex = Math.min(nextZombieIndexToSpawn + ZOMBIES_PER_SPAWN_BATCH, waveZombies.size());
        int spawnedInBatch = 0;
        
        log("Spawning batch: indices " + nextZombieIndexToSpawn + " to " + (endIndex-1) + " (total: " + totalZombiesInWave + ")");
        
        for (int i = nextZombieIndexToSpawn; i < endIndex; i++) {
            Zombie zombie = waveZombies.get(i);
            if (zombie != null) {
                zombie.setAlive(true);
                zombie.setGameManager(this);
                // Only start if not already started
                if (zombie.getState() == Thread.State.NEW) {
                    zombie.start();
                    spawnedInBatch++;
                } else {
                    log("Zombie #" + i + " already started (state: " + zombie.getState() + ")");
                }
            } else {
                log("WARNING: Zombie #" + i + " is null!");
            }
        }
        
        nextZombieIndexToSpawn = endIndex;
        log("Spawned batch of " + spawnedInBatch + " zombies (" + nextZombieIndexToSpawn + "/" + totalZombiesInWave + ")");
        
        // Update UI after spawning
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
    }

    public void stopZombieThreads() {
        System.out.println("⏹ Stopping all zombie threads...");
        for (Zombie zombie : waveZombies) {
            if (zombie != null) {
                zombie.setAlive(false);
                zombie.stopThread();
            }
        }
    }

    private void destroyLifeTree() {
        if (lifeTreePlaced != null) {
            board.deleteDefense(lifeTreePlaced);
        }

        baseHealth = 0;
        if (lifeTree != null) {
            lifeTree.setHealthPoints(0);
        }

        stopZombieThreads();

        lifeTreeRow = -1;
        lifeTreeColumn = -1;

        verifyLoss();
    }

    public boolean isValidPlacement(int row, int column) {
        return !matrixManager.isOccupied(row, column) && matrixManager.isValidDefensePosition(row, column);
    }

    public boolean isThereSpaceLeft(int totalSpace) {
        return defenseCostUsed < totalSpace;
    }

    public void verifyVictory() {
        // Only check if wave was generated - don't check roundActive 
        // because we want to detect victory even after combat ends
        if (!waveGenerated) {
            return;
        }
        
        // Prevent processing victory multiple times
        if (victoryProcessed) {
            return;
        }

        // Contar zombies vivos actualmente (spawneados)
        int aliveZombiesCount = 0;
        int unspawnedZombiesCount = 0;
        
        for (Zombie z : waveZombies) {
            if (z != null && z.isAlive() && z.getHealthPoints() > 0) {
                if (z.getState() == Thread.State.NEW) {
                    unspawnedZombiesCount++;
                } else {
                    aliveZombiesCount++;
                }
            }
        }
        
        // Note: We don't check board.getZombies().size() because it may have stale references
        // The waveZombies list is the authoritative source for zombie count

        // Solo hay victoria si NO quedan zombies vivos (ni spawneados ni por spawnear)
        if (aliveZombiesCount == 0 && unspawnedZombiesCount == 0) {
            log("===========================================");
            log("         VICTORY - LEVEL COMPLETE!        ");
            log("   All zombies have been defeated!       ");
            log("===========================================");
            
            // Mark victory as processed to prevent multiple dialogs
            victoryProcessed = true;
            
            // End combat log tracking
            if (combatLog != null) {
                combatLog.markRemainingEntitiesDead(true); // Mark all remaining zombies as dead
                combatLog.endBattle();
            }
            
            stopGame();
            showBattleSummary(true); // Show battle summary before victory dialog
        }
    }
    
    private void showBattleSummary(boolean hasWon) {
        // Prevent showing summary multiple times with Lock instead of synchronized
        summaryLock.lock();
        try {
            if (summaryShown) {
                log("⚠ Summary already shown, skipping duplicate call");
                return;
            }
            summaryShown = true;
            log("✓ Setting summaryShown = true, proceeding to show summary");
        } finally {
            summaryLock.unlock();
        }
        
        if (parentFrame == null || combatLog == null) {
            log("No parent frame or combat log, showing dialog directly");
            // Si no hay frame padre o combat log, mostrar directamente el diálogo
            if (hasWon) {
                showVictoryDialogDirect();
            } else {
                showGameOverDialogDirect(false);
            }
            return;
        }
        
        // Ensure we're on EDT before showing dialogs
        boolean isEDT = javax.swing.SwingUtilities.isEventDispatchThread();
        log("Current thread is EDT: " + isEDT);
        
        if (!isEDT) {
            log("Not on EDT, scheduling showBattleSummaryInternal via invokeLater");
            javax.swing.SwingUtilities.invokeLater(() -> showBattleSummaryInternal(hasWon));
        } else {
            log("Already on EDT, calling showBattleSummaryInternal directly");
            showBattleSummaryInternal(hasWon);
        }
    }
    
    private void showBattleSummaryInternal(boolean hasWon) {
        log(">>> showBattleSummaryInternal called, hasWon=" + hasWon);
        
        // Mostrar resumen de batalla (esto es MODAL, espera a que se cierre)
        Table.BattleSummaryDialog.showSummary(parentFrame, combatLog, hasWon);
        
        log(">>> BattleSummaryDialog closed, showing " + (hasWon ? "victory" : "defeat") + " dialog");
        
        // Después del resumen, mostrar el diálogo de victoria/derrota
        if (hasWon) {
            showVictoryDialogDirect();
        } else {
            showGameOverDialogDirect(false);
        }
    }
    
    private void showVictoryDialog() {
        if (parentFrame == null) {
            // ya clickeo el btn de avanzar = frame null
            advanceToNextRound();
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            showVictoryDialogDirect();
        });
    }
    
    private void showVictoryDialogDirect() {
        if (parentFrame == null) {
            advanceToNextRound();
            return;
        }
        
        Table.GameOverDialog.PlayerChoice choice = 
            Table.GameOverDialog.showGameOverDialog(parentFrame, true); // true = victoria
        
        handleVictoryChoice(choice);
    }
    
    private void handleVictoryChoice(Table.GameOverDialog.PlayerChoice choice) {
        switch (choice) {
            case RETRY_LEVEL:
                // Reintentar el nivel actual
                retryLevel();
                log("Restarting level " + level);
                break;
                
            case NEXT_LEVEL:
                // Avanzar al siguiente nivel
                advanceToNextRound();
                log("Advancing to level " + (level + 1));
                break;
                
            case RETURN_TO_MENU:
                // Volver al menú principal
                resetGame();
                if (sidePanel != null) {
                    sidePanel.returnToMenu();
                }
                break;
                
            case NONE:
            default:
                // Por defecto, avanzar al siguiente nivel
                advanceToNextRound();
                break;
        }
    }
    
    private void advanceToNextRound() {
        // DETENER TODOS LOS TIMERS primero
        stopGame();
        
        // Incrementar nivel
        level++;
        
        // Incrementar monedas (+5 por nivel según la descripción)
        coinsThisLevel = WaveManager.coinsForLevel(level);
        defenseCostLimit = coinsThisLevel;
        defenseCostUsed = 0;
        
        // Limpiar tablero pero mantener la configuración
        if (board != null) {
            board.clearZombies();
            board.clearDefenses();
            board.clearSelectedDefense();
        }
        
        waveZombies.clear();
        waveDefense.clear();
        matrixManager.restartMatrix();
        
        // Resetear estado del juego
        roundActive = false;
        waveGenerated = false;
        victoryProcessed = false; // Reset victory flag for new round
        lossProcessed = false; // Reset loss flag for new round
        summaryShown = false; // Reset summary flag for new round
        nextZombieIndexToSpawn = 0;
        zombiesRemaining = 0;
        isPaused = true; // Importante: marcar como pausado para permitir colocar defensas
        
        // Restaurar salud del Life Tree si existe
        if (lifeTree != null) {
            lifeTree.setHealthPoints(lifeTreeInitialHealth); // Usar salud inicial guardada
            baseHealth = lifeTreeInitialHealth;
        }
        
        // Resetear variables del Life Tree para que se pueda colocar de nuevo
        lifeTreePlaced = null;
        lifeTreeRow = -1;
        lifeTreeColumn = -1;
        
        // Mostrar Life Tree nuevamente en el catálogo
        if (sidePanel != null) {
            sidePanel.showLifeTreeInCatalog();
            sidePanel.enableStartButton(); // Habilitar el botón Start
            sidePanel.updateAllLabels();
        }
        
        log("===========================================");
        log("         LEVEL " + level + " STARTING...        ");
        log("   Coins available: " + coinsThisLevel);
        log("   Place your defenses and Life Tree!      ");
        log("===========================================");
    }

    public void verifyLoss() {
        // Prevent processing loss multiple times
        if (lossProcessed) {
            return;
        }

        if (baseHealth <= 0 || (lifeTree != null && lifeTree.getHealthPoints() <= 0)) {
            log("===========================================");
            log("         GAME OVER - YOU LOST!            ");
            log("   The Life Tree has been destroyed!      ");
            log("===========================================");
            
            // Mark loss as processed to prevent multiple dialogs
            lossProcessed = true;
            
            // End combat log tracking
            if (combatLog != null) {
                combatLog.markRemainingEntitiesDead(false); // Mark all remaining defenses as dead
                combatLog.endBattle();
            }
            
            stopGame();
            
            // Show battle summary before game over dialog
            showBattleSummary(false);
        }
    }
    
    private void showGameOverDialog(boolean hasWon) {
        if (parentFrame == null) {
            // Si no hay frame padre, solo resetear el juego
            resetGame();
            return;
        }
        
        // Mostrar el diálogo en el hilo de eventos de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            showGameOverDialogDirect(hasWon);
        });
    }
    
    private void showGameOverDialogDirect(boolean hasWon) {
        if (parentFrame == null) {
            resetGame();
            return;
        }
        
        Table.GameOverDialog.PlayerChoice choice = 
            Table.GameOverDialog.showGameOverDialog(parentFrame, hasWon);
        
        handlePlayerChoice(choice);
    }
    
    private void handlePlayerChoice(Table.GameOverDialog.PlayerChoice choice) {
        switch (choice) {
            case RETRY_LEVEL:
                // Reiniciar el nivel actual manteniendo las defensas
                retryLevel();
                log("Restarting level " + level);
                break;
                
            case NEXT_LEVEL:
                // Avanzar al siguiente nivel (solo si ganó)
                resetGame();
                level++;
                coinsThisLevel = WaveManager.coinsForLevel(level);
                defenseCostLimit = coinsThisLevel;
                if (sidePanel != null) {
                    sidePanel.showLifeTreeInCatalog(); // Mostrar Life Tree para el nuevo nivel
                    sidePanel.updateAllLabels();
                }
                log("Advancing to level " + level);
                break;
                
            case RETURN_TO_MENU:
                // Volver al menú principal
                resetGame();
                if (sidePanel != null) {
                    sidePanel.returnToMenu();
                }
                break;
                
            case NONE:
            default:
                // No hacer nada o resetear por defecto
                resetGame();
                break;
        }
    }
    
    /**
     * Retry current level - resets everything to start the same level again
     */
    private void retryLevel() {
        log("===========================================");
        log("         RETRYING LEVEL...                ");
        log("===========================================");

        stopGame();

        // Clear board visually
        if (board != null) {
            board.clearZombies();
            board.clearDefenses();
            board.clearSelectedDefense();
        }

        // Clear game state
        waveZombies.clear();
        waveDefense.clear();
        matrixManager.restartMatrix();

        // Reset Life Tree state
        lifeTree = null;
        lifeTreePlaced = null;
        lifeTreeRow = -1;
        lifeTreeColumn = -1;
        baseHealth = 100;
        
        // Reset defense placement
        defenseCostUsed = 0;
        selectedDefense = null;

        // Reset round state
        zombiesRemaining = 0;
        roundActive = false;
        waveGenerated = false;
        victoryProcessed = false; // Reset victory flag for retry
        lossProcessed = false; // Reset loss flag for retry
        summaryShown = false; // Reset summary flag for retry
        nextZombieIndexToSpawn = 0;

        // DON'T change level or coins - keep the same level
        coinsThisLevel = WaveManager.coinsForLevel(level);
        defenseCostLimit = coinsThisLevel;

        if (sidePanel != null) {
            sidePanel.updateAllLabels();
            sidePanel.enableStartButton();
            sidePanel.showLifeTreeInCatalog();
        }

        log("Level " + level + " ready to retry. Place your defenses again!");
    }

    private Image loadAndScale(String path) {

        if (path == null || path.isEmpty()) {
            return null;
        }
        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw == null) {
                return null;
            }
            int w = (int) (board.getWidth() / 25.0);
            int h = (int) (board.getHeight() / 25.0);
            if (w <= 0 || h <= 0) {
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

    public void setLevel(int level) {
        this.level = level;
        // Update coins and defense limit for the new level
        this.coinsThisLevel = WaveManager.coinsForLevel(level);
        this.defenseCostLimit = coinsThisLevel;
        
        // Initialize new combat log for this level
        this.combatLog = new CombatLog(level);
        
        // Update UI if available
        if (sidePanel != null) {
            sidePanel.updateAllLabels();
        }
        
        log("Level set to " + level + " - Coins: " + coinsThisLevel);
    }
    
    /**
     * Initialize a new combat log for the current round
     * Called at the start of each round to reset statistics
     */
    public void initializeCombatLog() {
        this.combatLog = new CombatLog(level);
        log("=== Combat Log Initialized for Level " + level + " ===");
        
        // Pre-register all existing defenses in the combat log
        int defensesRegistered = 0;
        for (Defense defense : waveDefense) {
            if (defense != null && combatLog != null) {
                // Create stats entry for each defense
                combatLog.getStats(defense);
                defensesRegistered++;
            }
        }
        log("Combat log: Registered " + defensesRegistered + " defenses");
    }
    
    /**
     * Register all zombies in combat log
     * Called after wave generation to ensure all zombies appear in stats
     */
    public void registerZombiesInCombatLog() {
        if (combatLog == null) {
            return;
        }
        
        int zombiesRegistered = 0;
        for (Zombie zombie : waveZombies) {
            if (zombie != null) {
                // Create stats entry for each zombie
                combatLog.getStats(zombie);
                zombiesRegistered++;
            }
        }
        log("Combat log: Registered " + zombiesRegistered + " zombies");
    }
    
    public void setParentFrame(javax.swing.JFrame frame) {
        this.parentFrame = frame;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public Defense getLifeTreeEntity() {
        return lifeTree;
    }

    public boolean isLifeTreePlaced() {
        return lifeTreePlaced != null;
    }

    public int getLifeTreeHealth() {
        return lifeTree != null ? lifeTree.getHealthPoints() : 0;
    }

    public int getLifeTreeRow() {
        return lifeTreeRow;
    }

    public int getLifeTreeColumn() {
        return lifeTreeColumn;
    }

    public Defense getSelectedDefense() {
        return selectedDefense;
    }

    public int getCoinsThisLevel() {
        return coinsThisLevel;
    }

    public void setCoinsThisLevel(int coinsThisLevel) {
        this.coinsThisLevel = coinsThisLevel;
    }

    public int getDefenseCostLimit() {
        return defenseCostLimit;
    }

    public void setDefenseCostLimit(int defenseCostLimit) {
        this.defenseCostLimit = defenseCostLimit;
    }

    public int getDefenseCostUsed() {
        return defenseCostUsed;
    }

    public void setDefenseCostUsed(int defenseCostUsed) {
        this.defenseCostUsed = defenseCostUsed;
    }

    public int getZombiesRemaining() {
        // Calculate dynamically to avoid sync issues
        int count = 0;
        for (Zombie z : waveZombies) {
            if (z != null && z.isAlive() && z.getHealthPoints() > 0) {
                count++;
            }
        }
        return count;
    }

    public void setZombiesRemaining(int zombiesRemaining) {
        // This method is kept for compatibility but the counter is now calculated dynamically
        // The zombiesRemaining field is no longer the source of truth
        this.zombiesRemaining = zombiesRemaining;
    }
    
    public int getTotalZombiesInWave() {
        return totalZombiesInWave;
    }
    
    public void setTotalZombiesInWave(int total) {
        this.totalZombiesInWave = total;
    }

    public void registerZombieDefeat(Zombie zombie) {
        if (zombie == null) {
            log("WARNING: registerZombieDefeat called with null zombie");
            return;
        }
        
        if (!zombie.isAlive()) {
            log("WARNING: registerZombieDefeat called for already dead zombie: " + zombie.getEntityName());
            return; // Ya fue contado
        }
        
        // Marcar como muerto
        zombie.setAlive(false);
        zombie.setHealthPoints(0); // Asegurar HP = 0
        board.deleteZombie(zombie);
        
        // No longer manually decrement counter - getZombiesRemaining() calculates it dynamically
        
        log("✓ Zombie defeated: " + zombie.getEntityName() + 
            " | Remaining: " + getZombiesRemaining() + 
            " | Thread state: " + zombie.getState());
        
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
        
        // Check for victory immediately after zombie defeat
        verifyVictory();
    }

    private boolean canAffordDefense(Defense defense) {
        if (defense == null) {
            return false;
        }
        int cost = Math.max(1, defense.getCost());
        return defenseCostUsed + cost <= defenseCostLimit;
    }

    private void startRound() {
        // Deseleccionar cualquier defensa activa al iniciar la ronda
        if (selectedDefense != null) {
            selectedDefense = null;
            board.clearSelectedDefense();
            if (sidePanel != null) {
                sidePanel.deselectDefense();
            }
        }
        
        // Start threads for all placed defenses
        startDefenseThreads();
        
        waveManager.startRound();
    }
    
    /**
     * Start threads for all defenses
     */
    private void startDefenseThreads() {
        System.out.println("▶ Starting defense threads...");
        for (Defense defense : waveDefense) {
            if (defense != null && defense.getHealthPoints() > 0) {
                defense.setGameManager(this);
                defense.startThread();
            }
        }
    }

    public void resetGame() {
        log("===========================================");
        log("         RESETTING GAME...                ");
        log("===========================================");

        stopGame();

        if (board != null) {
            board.clearZombies();
            board.clearDefenses();
            board.clearSelectedDefense();
        }

        waveZombies.clear();
        waveDefense.clear();
        matrixManager.restartMatrix();

        level = 1;
        baseHealth = 100;
        coinsThisLevel = WaveManager.coinsForLevel(level);
        defenseCostLimit = coinsThisLevel;
        defenseCostUsed = 0;
        zombiesRemaining = 0;
        roundActive = false;
        waveGenerated = false;
        victoryProcessed = false; // Reset victory flag
        lossProcessed = false; // Reset loss flag
        summaryShown = false; // Reset summary flag
        nextZombieIndexToSpawn = 0;
        selectedDefense = null;

        lifeTree = null;
        lifeTreePlaced = null;
        lifeTreeRow = -1;
        lifeTreeColumn = -1;

        if (sidePanel != null) {
            sidePanel.showLifeTreeInCatalog(); // Asegurar que el Life Tree esté visible
            sidePanel.updateAllLabels();
            sidePanel.enableStartButton(); // Reactivar el botón de Start
        }

        log("Game reset complete. Place the Life Tree to start again.");
    }

    public boolean isGamePaused() {
        return isPaused;
    }

    public boolean isRoundActive() {
        return roundActive;
    }

    public void setRoundActive(boolean roundActive) {
        this.roundActive = roundActive;
    }

    public boolean isWaveGenerated() {
        return waveGenerated;
    }

    public void setWaveGenerated(boolean waveGenerated) {
        this.waveGenerated = waveGenerated;
    }

    public ArrayList<Zombie> getWaveZombiesInternal() {
        return waveZombies;
    }

    public ArrayList<Defense> getWaveDefenseInternal() {
        return waveDefense;
    }

    public GameBoard getBoard() {
        return board;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public ConfigManager getConfigManager() {
        return configMg;
    }

    public Random getRandomGenerator() {
        return rnd;
    }
    
    public CombatLog getCombatLog() {
        return combatLog;
    }

    public MatrixManager getMatrixManager() {
        return matrixManager;
    }
    
    /**
     * Logs a message to the side panel
     */
    private void log(String message) {
        if (sidePanel != null) {
            sidePanel.appendLog(message);
        }
    }
    
    /**
     * Clones a defense for placement (creates a new instance with same properties)
     */
    private Defense cloneDefense(Defense original) {
        if (original == null) {
            return null;
        }
        
        Defense cloned = null;
        
        try {
            // Use the types-based constructor for proper hybrid type support
            ArrayList<DefenseType> types = original.getTypes();
            
            // Check specific defense types first
            if (original instanceof DefenseMultipleAttack) {
                DefenseMultipleAttack origMulti = (DefenseMultipleAttack) original;
                cloned = new DefenseMultipleAttack(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origMulti.getAttack(),
                    0, // range is auto-calculated
                    origMulti.getAmtOfAttacks()
                );
            } else if (original instanceof DefenseHealer) {
                DefenseHealer origHealer = (DefenseHealer) original;
                cloned = new DefenseHealer(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origHealer.getHealPower()
                );
            } else if (original instanceof DefenseExplosive) {
                cloned = new DefenseExplosive(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    0 // range is auto-calculated
                );
            } else if (original instanceof DefenseFlying) {
                DefenseFlying origFlying = (DefenseFlying) original;
                cloned = new DefenseFlying(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origFlying.getAttack(),
                    0 // range is auto-calculated
                );
            } else if (original instanceof DefenseContact) {
                DefenseContact origContact = (DefenseContact) original;
                cloned = new DefenseContact(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origContact.getAttack()
                );
            } else if (original instanceof DefenseMediumRange) {
                DefenseMediumRange origMedium = (DefenseMediumRange) original;
                cloned = new DefenseMediumRange(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origMedium.getAttack(),
                    0 // range is auto-calculated
                );
            } else if (original instanceof DefenseAttacker) {
                // Generic attacker
                DefenseAttacker origAttacker = (DefenseAttacker) original;
                cloned = new DefenseAttacker(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origAttacker.getAttack(),
                    0 // range is auto-calculated
                );
            } else {
                // Generic defense or BLOCKS
                cloned = new Defense(
                    types,
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost()
                );
            }
            
            // Copy image path
            if (original.getImagePath() != null) {
                cloned.setImagePath(original.getImagePath());
            }
            
        } catch (Exception e) {
            System.err.println("Error cloning defense: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cloned;
    }
    
    // ==================== LEVEL SCALING SYSTEM ====================
    
    /**
     * Applies level scaling to a defense (increases HP and damage by 5-20% per level)
     */
    private void applyDefenseScaling(Defense defense) {
        if (defense == null || level <= 1) {
            return; // No scaling for level 1
        }
        
        // Each level adds a random 5-20% increase
        double totalMultiplier = 1.0;
        for (int i = 1; i < level; i++) {
            double increase = 0.05 + (rnd.nextDouble() * 0.15); // 5-20%
            totalMultiplier *= (1.0 + increase);
        }
        
        // Scale HP
        int originalHP = defense.getHealthPoints();
        int scaledHP = (int) Math.round(originalHP * totalMultiplier);
        defense.setHealthPoints(scaledHP);
        
        // Scale damage for attacker types
        if (defense instanceof DefenseAttacker) {
            DefenseAttacker attacker = (DefenseAttacker) defense;
            int originalDamage = attacker.getAttack();
            int scaledDamage = (int) Math.round(originalDamage * totalMultiplier);
            attacker.setAttack(scaledDamage);
            
            log("Defense scaled (Lvl " + level + "): " + defense.getEntityName() + 
                " | HP: " + originalHP + " → " + scaledHP + 
                " | Damage: " + originalDamage + " → " + scaledDamage +
                " (×" + String.format("%.2f", totalMultiplier) + ")");
        } else if (defense instanceof DefenseHealer) {
            // Scale heal power for healers
            DefenseHealer healer = (DefenseHealer) defense;
            int originalHeal = healer.getHealPower();
            int scaledHeal = (int) Math.round(originalHeal * totalMultiplier);
            healer.setHealPower(scaledHeal);
            
            log("Defense scaled (Lvl " + level + "): " + defense.getEntityName() + 
                " | HP: " + originalHP + " → " + scaledHP + 
                " | Heal: " + originalHeal + " → " + scaledHeal +
                " (×" + String.format("%.2f", totalMultiplier) + ")");
        } else {
            log("Defense scaled (Lvl " + level + "): " + defense.getEntityName() + 
                " | HP: " + originalHP + " → " + scaledHP +
                " (×" + String.format("%.2f", totalMultiplier) + ")");
        }
    }
    
    /**
     * Applies level scaling to a zombie (increases HP and damage by 5-20% per level)
     */
    void applyZombieScaling(Zombie zombie) {
        if (zombie == null || level <= 1) {
            return; // No scaling for level 1
        }
        
        // Each level adds a random 5-20% increase
        double totalMultiplier = 1.0;
        for (int i = 1; i < level; i++) {
            double increase = 0.05 + (rnd.nextDouble() * 0.15); // 5-20%
            totalMultiplier *= (1.0 + increase);
        }
        
        // Scale HP
        int originalHP = zombie.getHealthPoints();
        int scaledHP = (int) Math.round(originalHP * totalMultiplier);
        zombie.setHealthPoints(scaledHP);
        
        // Scale damage for attacker types
        if (zombie instanceof ZombieAttacker) {
            ZombieAttacker attacker = (ZombieAttacker) zombie;
            int originalDamage = attacker.getDamage();
            int scaledDamage = (int) Math.round(originalDamage * totalMultiplier);
            attacker.setDamage(scaledDamage);
            
            log("Zombie scaled (Lvl " + level + "): " + zombie.getEntityName() + 
                " | HP: " + originalHP + " → " + scaledHP + 
                " | Damage: " + originalDamage + " → " + scaledDamage +
                " (×" + String.format("%.2f", totalMultiplier) + ")");
        } else if (zombie instanceof ZombieHealer) {
            // Scale heal power for healers
            ZombieHealer healer = (ZombieHealer) zombie;
            int originalHeal = healer.getHealPower();
            int scaledHeal = (int) Math.round(originalHeal * totalMultiplier);
            healer.setHealPower(scaledHeal);
            
            log("Zombie scaled (Lvl " + level + "): " + zombie.getEntityName() + 
                " | HP: " + originalHP + " → " + scaledHP + 
                " | Heal: " + originalHeal + " → " + scaledHeal +
                " (×" + String.format("%.2f", totalMultiplier) + ")");
        } else {
            log("Zombie scaled (Lvl " + level + "): " + zombie.getEntityName() + 
                " | HP: " + originalHP + " → " + scaledHP +
                " (×" + String.format("%.2f", totalMultiplier) + ")");
        }
    }
    
    // ==================== COMBAT SYSTEM ====================
    
    /**
     * Processes combat for all entities (called every second by combatTimer)
     */
    private void processCombat() {
        if (isPaused || !roundActive) {
            return;
        }
        
        log("=== COMBAT TICK === Defenses: " + waveDefense.size() + " | Zombies: " + waveZombies.size());
        
        // Process defense attacks - usar una copia para evitar ConcurrentModificationException
        ArrayList<Defense> defensesToProcess = new ArrayList<>(waveDefense);
        for (Defense defense : defensesToProcess) {
            if (defense == null || defense.getHealthPoints() <= 0) {
                continue; // Skip dead/null defenses
            }
            
            if (defense.isHealer()) {
                processHealing(defense);
            } else {
                processDefenseAttack(defense);
            }
        }
        
        // Process zombie attacks - usar una copia para evitar ConcurrentModificationException
        ArrayList<Zombie> zombiesToProcess = new ArrayList<>(waveZombies);
        for (Zombie zombie : zombiesToProcess) {
            if (zombie == null || !zombie.isAlive() || zombie.getHealthPoints() <= 0) {
                continue; // Skip dead/null zombies
            }
            
            if (zombie.isHealer()) {
                processHealing(zombie);
            } else {
                processZombieAttack(zombie);
            }
        }
        
        // Remove dead entities
        removeDeadEntities();
        
        // Update UI after combat
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
        
        // Check for victory/loss after combat processing
        verifyVictory();
        verifyLoss();
    }
    
    // ==================== THREADED COMBAT METHODS ====================
    
    /**
     * Process defense attack from its own thread (using ReentrantLock instead of synchronized)
     */
    public void processDefenseAttackThreaded(Defense defense) {
        combatLock.lock();
        try {
            if (defense == null || defense.getHealthPoints() <= 0) {
                return;
            }
            
            if (defense.isHealer()) {
                processHealing(defense);
            } else {
                processDefenseAttack(defense);
            }
        } finally {
            combatLock.unlock();
        }
    }
    
    /**
     * Process zombie attack from its own thread (using ReentrantLock instead of synchronized)
     */
    public void processZombieAttackThreaded(Zombie zombie) {
        combatLock.lock();
        try {
            if (zombie == null || !zombie.isAlive() || zombie.getHealthPoints() <= 0) {
                return;
            }
            
            if (zombie.isHealer()) {
                processHealing(zombie);
            } else {
                processZombieAttack(zombie);
            }
        } finally {
            combatLock.unlock();
        }
    }
    
    /**
     * Processes attack for a single defense
     */
    private void processDefenseAttack(Defense defense) {
        // Blocks don't attack
        if (defense.hasType(DefenseType.BLOCKS)) {
            return;
        }
        
        // Get all valid zombie targets in range
        ArrayList<Entity> zombieEntities = new ArrayList<>();
        for (Zombie z : waveZombies) {
            if (z.isAlive() && z.getHealthPoints() > 0) {
                zombieEntities.add(z);
            }
        }
        
        ArrayList<Entity> validTargets = CombatRules.getValidTargets(defense, zombieEntities);
        
        if (validTargets.isEmpty()) {
            return; // No targets in range
        }
        
        // Check if explosive defense should explode (when in contact)
        if (defense.isExplosive()) {
            for (Entity target : validTargets) {
                if (CombatRules.shouldExplode(defense, target)) {
                    explodeDefense(defense, validTargets);
                    return; // Defense explodes and is destroyed
                }
            }
        }
        
        // Get attack damage
        int damage = getEntityDamage(defense);
        boolean attackPerformed = false;
        
        // Handle multiple attacks (MULTIPLEATTACK type)
        if (defense.hasMultipleAttacks()) {
            DefenseMultipleAttack multiDef = (DefenseMultipleAttack) defense;
            int attackCount = multiDef.getAmtOfAttacks();
            
            // Attack up to 2 different targets, but attackCount times each
            int maxTargets = Math.min(2, validTargets.size());
            ArrayList<Entity> closestTargets = findClosestEntities(defense, validTargets, maxTargets);
            
            // Distribute attacks among the closest targets
            for (int i = 0; i < attackCount; i++) {
                Entity target = closestTargets.get(i % closestTargets.size());
                int distance = CombatRules.calculateDistance(defense, target);
                log(defense.getEntityName() + " (range:" + defense.getAttackRange() + 
                    ") multi-attacks [" + (i+1) + "/" + attackCount + "] " + target.getEntityName() + 
                    " at distance " + distance + " for " + damage + " damage");
                
                // Log the attack in combat log
                if (combatLog != null) {
                    combatLog.logAttack(defense, target, damage);
                }
                
                applyDamage(target, damage, defense);
                attackPerformed = true;
            }
        } else {
            // Normal attack: attack up to 2 closest targets
            int maxTargets = Math.min(2, validTargets.size());
            ArrayList<Entity> closestTargets = findClosestEntities(defense, validTargets, maxTargets);
            
            for (Entity target : closestTargets) {
                int distance = CombatRules.calculateDistance(defense, target);
                log(defense.getEntityName() + " (range:" + defense.getAttackRange() + 
                    ") attacks " + target.getEntityName() + " at distance " + distance + 
                    " for " + damage + " damage");
                
                // Log the attack in combat log
                if (combatLog != null) {
                    combatLog.logAttack(defense, target, damage);
                }
                
                applyDamage(target, damage, defense);
                attackPerformed = true;
            }
        }
        
        // If explosive defense attacked successfully, it explodes
        if (defense.isExplosive() && attackPerformed) {
            log(defense.getEntityName() + " is explosive and attacked - triggering explosion!");
            explodeDefense(defense, validTargets);
        }
    }
    
    /**
     * Processes attack for a single zombie
     */
    private void processZombieAttack(Zombie zombie) {
        // Get all valid defense targets in range
        ArrayList<Entity> defenseEntities = new ArrayList<>();
        for (Defense d : waveDefense) {
            if (d.getHealthPoints() > 0) {
                defenseEntities.add(d);
            }
        }
        
        // Add life tree as potential target
        if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
            defenseEntities.add(lifeTree);
        }
        
        ArrayList<Entity> validTargets = CombatRules.getValidTargets(zombie, defenseEntities);
        
        if (validTargets.isEmpty()) {
            return; // No targets in range, zombie keeps moving
        }
        
        // Check if explosive zombie should explode (when in contact)
        if (zombie.isExplosive()) {
            for (Entity target : validTargets) {
                if (CombatRules.shouldExplode(zombie, target)) {
                    explodeZombie(zombie, validTargets);
                    return; // Zombie explodes and dies
                }
            }
        }
        
        // Get attack damage
        int damage = getEntityDamage(zombie);
        
        // Zombies attack up to 2 closest targets
        int maxTargets = Math.min(2, validTargets.size());
        ArrayList<Entity> closestTargets = findClosestEntities(zombie, validTargets, maxTargets);
        
        for (Entity target : closestTargets) {
            int distance = CombatRules.calculateDistance(zombie, target);
            log(zombie.getEntityName() + " (range:" + zombie.getAttackRange() + 
                ") attacks " + target.getEntityName() + " at distance " + distance + 
                " for " + damage + " damage");
            
            // Log the attack in combat log
            if (combatLog != null) {
                combatLog.logAttack(zombie, target, damage);
            }
            
            applyDamage(target, damage, zombie);
            
            // If explosive zombie attacked successfully, it explodes (after first hit)
            if (zombie.isExplosive()) {
                log(zombie.getEntityName() + " is explosive and attacked - triggering explosion!");
                explodeZombie(zombie, validTargets);
                return; // Zombie dies after exploding
            }
        }
    }
    
    /**
     * Processes healing for a healer entity
     */
    private void processHealing(Entity healer) {
        // Get all valid heal targets
        ArrayList<Entity> candidates = new ArrayList<>();
        
        if (healer instanceof Defense) {
            // Defense healer can heal other defenses
            for (Defense d : waveDefense) {
                if (d.getHealthPoints() > 0) {
                    candidates.add(d);
                }
            }
            // Can also heal life tree
            if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
                candidates.add(lifeTree);
            }
        } else if (healer instanceof Zombie) {
            // Zombie healer can heal other zombies
            for (Zombie z : waveZombies) {
                if (z.isAlive() && z.getHealthPoints() > 0) {
                    candidates.add(z);
                }
            }
        }
        
        ArrayList<Entity> validTargets = CombatRules.getValidHealTargets(healer, candidates);
        
        if (validTargets.isEmpty()) {
            return; // No one to heal
        }
        
        // Heal the most damaged ally (healers heal 1 ally at a time)
        Entity mostDamaged = findMostDamagedEntity(validTargets);
        if (mostDamaged != null) {
            int healAmount = getEntityHealPower(healer);
            
            // Log the healing in combat log
            if (combatLog != null) {
                combatLog.logHeal(healer, mostDamaged, healAmount);
            }
            
            applyHealing(mostDamaged, healAmount);
        }
    }
    
    /**
     * Explodes a defense, dealing instant kill to all zombies in 3x3 area
     */
    private void explodeDefense(Defense defense, ArrayList<Entity> triggeredBy) {
        final int EXPLOSION_RADIUS = 1; // 3x3 area (radius 1 from center)
        
        // Encontrar TODOS los zombies en el área de explosión 3x3
        ArrayList<Zombie> zombiesInExplosionArea = new ArrayList<>();
        int defRow = defense.getCurrentRow();
        int defCol = defense.getCurrentColumn();
        
        for (Zombie z : waveZombies) {
            if (z == null || !z.isAlive() || z.getHealthPoints() <= 0) {
                continue;
            }
            
            int distance = CombatRules.calculateDistance(defense, z);
            if (distance <= EXPLOSION_RADIUS) {
                zombiesInExplosionArea.add(z);
            }
        }
        
        log("=== EXPLOSION === Defense " + defense.getEntityName() + 
            " exploded at (" + defRow + "," + defCol + ")! Zombies in 3x3 area: " + zombiesInExplosionArea.size());
        
        // Convert to Entity list for logging
        ArrayList<Entity> explosionTargets = new ArrayList<>(zombiesInExplosionArea);
        
        // Matar a todos los zombies en el área PRIMERO
        for (Zombie zombie : zombiesInExplosionArea) {
            log("  - Exploding on zombie: " + zombie.getEntityName() + 
                " at (" + zombie.getCurrentRow() + "," + zombie.getCurrentColumn() + ")" +
                " (HP: " + zombie.getHealthPoints() + ", Alive: " + zombie.isAlive() + ")");
            applyDamage(zombie, 999999, defense); // Instant kill
        }
        
        // Log explosion event DESPUÉS de aplicar el daño
        if (combatLog != null) {
            combatLog.logExplosion(defense, explosionTargets, 999999);
        }
        
        // Defense is destroyed
        defense.setHealthPoints(0);
        removeDefenseFromBoard(defense);
        log("=== EXPLOSION END === " + defense.getEntityName() + " removed from board");
    }
    
    /**
     * Explodes a zombie, dealing instant kill to all defenses in 3x3 area
     */
    private void explodeZombie(Zombie zombie, ArrayList<Entity> triggeredBy) {
        final int EXPLOSION_RADIUS = 1; // 3x3 area (radius 1 from center)
        
        // Encontrar TODAS las defensas en el área de explosión 3x3
        ArrayList<Defense> defensesInExplosionArea = new ArrayList<>();
        int zombieRow = zombie.getCurrentRow();
        int zombieCol = zombie.getCurrentColumn();
        
        log("=== EXPLOSION START === Zombie " + zombie.getEntityName() + 
            " at (" + zombieRow + "," + zombieCol + ") | Flying: " + zombie.isFlying());
        
        for (Defense d : waveDefense) {
            if (d == null || d.getHealthPoints() <= 0) {
                continue;
            }
            
            int distance = CombatRules.calculateDistance(zombie, d);
            log("  - Checking defense: " + d.getEntityName() + 
                " at (" + d.getCurrentRow() + "," + d.getCurrentColumn() + ")" +
                " | Flying: " + d.isFlying() +
                " | Distance: " + distance +
                " | HP: " + d.getHealthPoints());
            
            if (distance <= EXPLOSION_RADIUS) {
                defensesInExplosionArea.add(d);
                log("    ✓ ADDED to explosion targets");
            }
        }
        
        // También incluir Life Tree si está en rango
        if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
            int distance = CombatRules.calculateDistance(zombie, lifeTree);
            if (distance <= EXPLOSION_RADIUS) {
                defensesInExplosionArea.add(lifeTree);
                log("  - Life Tree in range, added to targets");
            }
        }
        
        log("=== EXPLOSION === Total defenses in 3x3 area: " + defensesInExplosionArea.size());
        
        // Convert to Entity list for logging
        ArrayList<Entity> explosionTargets = new ArrayList<>(defensesInExplosionArea);
        
        // Destruir todas las defensas en el área PRIMERO
        for (Defense defense : defensesInExplosionArea) {
            log("  - Exploding on defense: " + defense.getEntityName() + 
                " at (" + defense.getCurrentRow() + "," + defense.getCurrentColumn() + ")" +
                " (HP: " + defense.getHealthPoints() + ")");
            applyDamage(defense, 999999, zombie); // Instant kill
        }
        
        // Log explosion event DESPUÉS de aplicar el daño
        if (combatLog != null) {
            combatLog.logExplosion(zombie, explosionTargets, 999999);
        }
        
        // Zombie dies
        zombie.setHealthPoints(0);
        zombie.setAlive(false);
        
        // Log zombie's own death (suicide by explosion)
        if (combatLog != null) {
            combatLog.logDeath(zombie, zombie); // Killed by itself
        }
        
        registerZombieDefeat(zombie);
        log("=== EXPLOSION END === " + zombie.getEntityName() + " died from explosion");
    }
    
    /**
     * Applies damage to an entity
     */
    private void applyDamage(Entity target, int damage, Entity attacker) {
        int currentHealth = target.getHealthPoints();
        int newHealth = Math.max(0, currentHealth - damage);
        target.setHealthPoints(newHealth);
        
        // Update combat log with new health
        if (combatLog != null) {
            combatLog.updateEntityHealth(target);
        }
        
        log(target.getEntityName() + " took " + damage + " damage! HP: " + currentHealth + " -> " + newHealth);
        
        if (newHealth <= 0) {
            log(target.getEntityName() + " has been defeated!");
            
            if (target instanceof Zombie) {
                Zombie z = (Zombie) target;
                z.setAlive(false);
                
                // Log zombie death
                if (combatLog != null && attacker != null) {
                    combatLog.logDeath(z, attacker);
                }
                
                registerZombieDefeat(z);
            } else if (target instanceof Defense) {
                Defense d = (Defense) target;
                // Log defense death
                if (combatLog != null && attacker != null) {
                    combatLog.logDeath(d, attacker);
                }
                
                if (d == lifeTree) {
                    destroyLifeTree();
                } else {
                    removeDefenseFromBoard(d);
                }
            }
        }
    }
    
    /**
     * Applies healing to an entity
     */
    private void applyHealing(Entity target, int healAmount) {
        int currentHealth = target.getHealthPoints();
        int newHealth = currentHealth + healAmount;
        // Note: In future, cap healing at max health
        target.setHealthPoints(newHealth);
        
        log(target.getEntityName() + " was healed for " + healAmount + " HP");
    }
    
    /**
     * Gets the attack damage of an entity
     */
    private int getEntityDamage(Entity entity) {
        if (entity instanceof EntityAttacker) {
            if (entity instanceof ZombieAttacker) {
                return ((ZombieAttacker) entity).getDamage();
            } else if (entity instanceof DefenseAttacker) {
                return ((DefenseAttacker) entity).getAttack();
            }
        }
        return 1; // Default damage
    }
    
    /**
     * Gets the healing power of a healer entity
     */
    private int getEntityHealPower(Entity entity) {
        if (entity instanceof EntityHealer) {
            if (entity instanceof ZombieHealer) {
                return ((ZombieHealer) entity).getHealPower();
            } else if (entity instanceof DefenseHealer) {
                return ((DefenseHealer) entity).getHealPower();
            }
        }
        return 5; // Default heal amount
    }
    
    /**
     * Finds the closest entity from a list
     */
    private Entity findClosestEntity(Entity source, ArrayList<Entity> candidates) {
        Entity closest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Entity candidate : candidates) {
            int distance = CombatRules.calculateDistance(source, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                closest = candidate;
            }
        }
        
        return closest;
    }
    
    /**
     * Finds the N closest entities to the source
     */
    private ArrayList<Entity> findClosestEntities(Entity source, ArrayList<Entity> candidates, int maxCount) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Create list with distances
        ArrayList<EntityDistance> distances = new ArrayList<>();
        for (Entity candidate : candidates) {
            int distance = CombatRules.calculateDistance(source, candidate);
            distances.add(new EntityDistance(candidate, distance));
        }
        
        // Sort by distance
        distances.sort((a, b) -> Integer.compare(a.distance, b.distance));
        
        // Return up to maxCount closest entities
        ArrayList<Entity> result = new ArrayList<>();
        for (int i = 0; i < Math.min(maxCount, distances.size()); i++) {
            result.add(distances.get(i).entity);
        }
        
        return result;
    }
    
    /**
     * Helper class to store entity with distance
     */
    private static class EntityDistance {
        Entity entity;
        int distance;
        
        EntityDistance(Entity entity, int distance) {
            this.entity = entity;
            this.distance = distance;
        }
    }
    
    /**
     * Finds the most damaged entity from a list (for healing priority)
     */
    private Entity findMostDamagedEntity(ArrayList<Entity> candidates) {
        Entity mostDamaged = null;
        int lowestHealth = Integer.MAX_VALUE;
        
        for (Entity candidate : candidates) {
            int health = candidate.getHealthPoints();
            if (health < lowestHealth) {
                lowestHealth = health;
                mostDamaged = candidate;
            }
        }
        
        return mostDamaged;
    }
    
    /**
     * Removes a defense from the board
     */
    private void removeDefenseFromBoard(Defense defense) {
        int row = defense.getCurrentRow();
        int col = defense.getCurrentColumn();
        
        if (row >= 0 && col >= 0) {
            matrixManager.free(row, col);
            board.removePlacedDefense(row, col);
        }
        
        waveDefense.remove(defense);
    }
    
    /**
     * Removes dead entities from the game
     */
    private void removeDeadEntities() {
        // Remove dead zombies - but ONLY those that have been spawned already
        // A zombie is "spawned" if its thread state is not NEW
        ArrayList<Zombie> zombiesToRemove = new ArrayList<>();
        
        for (Zombie zombie : waveZombies) {
            if (zombie == null) {
                zombiesToRemove.add(zombie);
                continue;
            }
            
            // Don't remove zombies that haven't been spawned yet
            if (zombie.getState() == Thread.State.NEW) {
                continue;
            }
            
            // Remove if dead or no health
            if (!zombie.isAlive() || zombie.getHealthPoints() <= 0) {
                zombiesToRemove.add(zombie);
                
                // Log death event only if not already logged
                // (Check if stats exist and if died flag is not set)
                if (combatLog != null) {
                    GameLogic.CombatLog.EntityCombatStats stats = combatLog.getStats(zombie);
                    if (stats != null && !stats.died) {
                        combatLog.logDeath(zombie, null);
                    }
                }
            }
        }
        
        if (!zombiesToRemove.isEmpty()) {
            waveZombies.removeAll(zombiesToRemove);
            // Silently remove - don't spam logs
            // log("Removed " + zombiesToRemove.size() + " dead zombies from wave list");
        }
        
        // Remove dead defenses
        ArrayList<Defense> defensesToRemove = new ArrayList<>();
        
        for (Defense defense : waveDefense) {
            if (defense == null || defense.getHealthPoints() <= 0) {
                defensesToRemove.add(defense);
                
                // Log death event only if not already logged
                if (combatLog != null) {
                    GameLogic.CombatLog.EntityCombatStats stats = combatLog.getStats(defense);
                    if (stats != null && !stats.died) {
                        combatLog.logDeath(defense, null);
                    }
                }
            }
        }
        
        if (!defensesToRemove.isEmpty()) {
            waveDefense.removeAll(defensesToRemove);
            // Silently remove - don't spam logs
            // log("Removed " + defensesToRemove.size() + " dead defenses from wave list");
        }
    }
}
