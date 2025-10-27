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
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.Timer;


public class GameManager {

    private static final String LIFE_TREE_NAME = "LIFE TREE";
    private static final int ZOMBIES_PER_SPAWN_BATCH = 5;
    private static final int SPAWN_DELAY_MS = 2000; // 2 segundos entre grupos

    private final GameBoard board;
    private final SidePanel sidePanel;
    private final MatrixManager matrixManager;
    private final ConfigManager configMg;
    private final Random rnd;
    private final WaveManager waveManager;
    private final ZombieMovementController movementController;
    private final ArrayList<Zombie> waveZombies;
    private final ArrayList<Defense> waveDefense;
    
    private javax.swing.JFrame parentFrame; // Para mostrar diálogos

    private Timer gameTimer;
    private Timer zombieSpawnTimer;
    private Timer combatTimer;
    private int nextZombieIndexToSpawn;
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
        this.nextZombieIndexToSpawn = 0;
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
        
        log("Spawning batch: indices " + nextZombieIndexToSpawn + " to " + (endIndex-1) + " (total: " + waveZombies.size() + ")");
        
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
        log("Spawned batch of " + spawnedInBatch + " zombies (" + nextZombieIndexToSpawn + "/" + waveZombies.size() + ")");
        
        // Update UI after spawning
        if (sidePanel != null) {
            sidePanel.refreshStatusCounters();
        }
    }

    public void stopZombieThreads() {
        for (Zombie zombie : waveZombies) {
            if (zombie != null) {
                zombie.setAlive(false);
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
            
            stopGame();
            showVictoryDialog();
        }
    }
    
    private void showVictoryDialog() {
        if (parentFrame == null) {
            // ya clickeo el btn de avanzar = frame null
            advanceToNextRound();
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            Table.GameOverDialog.PlayerChoice choice = 
                Table.GameOverDialog.showGameOverDialog(parentFrame, true); // true = victoria
            
            handleVictoryChoice(choice);
        });
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

        if (baseHealth <= 0 || (lifeTree != null && lifeTree.getHealthPoints() <= 0)) {
            log("===========================================");
            log("         GAME OVER - YOU LOST!            ");
            log("   The Life Tree has been destroyed!      ");
            log("===========================================");
            stopGame();
            
            // Mostrar diálogo de Game Over
            showGameOverDialog(false);
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
            Table.GameOverDialog.PlayerChoice choice = 
                Table.GameOverDialog.showGameOverDialog(parentFrame, hasWon);
            
            handlePlayerChoice(choice);
        });
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
        
        waveManager.startRound();
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
        nextZombieIndexToSpawn = 0;
        selectedDefense = null;

        lifeTree = null;
        lifeTreePlaced = null;
        lifeTreeRow = -1;
        lifeTreeColumn = -1;

        if (sidePanel != null) {
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
            // Check specific defense types first
            if (original instanceof DefenseMultipleAttack) {
                DefenseMultipleAttack origMulti = (DefenseMultipleAttack) original;
                cloned = new DefenseMultipleAttack(
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origMulti.getAttack(),
                    origMulti.getRange(),
                    origMulti.getAmtOfAttacks()
                );
            } else if (original instanceof DefenseHealer) {
                DefenseHealer origHealer = (DefenseHealer) original;
                cloned = new DefenseHealer(
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origHealer.getHealPower()
                );
            } else if (original instanceof DefenseExplosive) {
                DefenseExplosive origExplosive = (DefenseExplosive) original;
                cloned = new DefenseExplosive(
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origExplosive.getRange()
                );
            } else if (original instanceof DefenseFlying) {
                DefenseFlying origFlying = (DefenseFlying) original;
                cloned = new DefenseFlying(
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origFlying.getAttack(),
                    origFlying.getRange()
                );
            } else if (original instanceof DefenseContact) {
                DefenseContact origContact = (DefenseContact) original;
                cloned = new DefenseContact(
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origContact.getAttack()
                );
            } else if (original instanceof DefenseMediumRange) {
                DefenseMediumRange origMedium = (DefenseMediumRange) original;
                cloned = new DefenseMediumRange(
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origMedium.getAttack(),
                    origMedium.getRange()
                );
            } else if (original instanceof DefenseAttacker) {
                // Generic attacker
                DefenseAttacker origAttacker = (DefenseAttacker) original;
                cloned = new DefenseAttacker(
                    original.getType(),
                    original.getEntityName(),
                    original.getHealthPoints(),
                    original.getShowUpLevel(),
                    original.getCost(),
                    origAttacker.getAttack(),
                    origAttacker.getRange()
                );
            } else {
                // Generic defense or BLOCKS
                cloned = new Defense(
                    original.getType(),
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
        List<Defense> defensesToProcess = new ArrayList<>(waveDefense);
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
        List<Zombie> zombiesToProcess = new ArrayList<>(waveZombies);
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
    
    /**
     * Processes attack for a single defense
     */
    private void processDefenseAttack(Defense defense) {
        // Blocks don't attack
        if (defense.getType() == DefenseType.BLOCKS) {
            return;
        }
        
        // Get all valid zombie targets in range
        List<Entity> zombieEntities = new ArrayList<>();
        for (Zombie z : waveZombies) {
            if (z.isAlive() && z.getHealthPoints() > 0) {
                zombieEntities.add(z);
            }
        }
        
        List<Entity> validTargets = CombatRules.getValidTargets(defense, zombieEntities);
        
        if (validTargets.isEmpty()) {
            return; // No targets in range
        }
        
        // Check if explosive defense should explode
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
        
        // Handle multiple attacks
        if (defense.hasMultipleAttacks()) {
            DefenseMultipleAttack multiDef = (DefenseMultipleAttack) defense;
            int attackCount = multiDef.getAmtOfAttacks();
            
            // Distribute attacks among targets
            for (int i = 0; i < attackCount && !validTargets.isEmpty(); i++) {
                Entity target = validTargets.get(i % validTargets.size());
                int distance = CombatRules.calculateDistance(defense, target);
                log(defense.getEntityName() + " (range:" + defense.getAttackRange() + 
                    ") attacks " + target.getEntityName() + " at distance " + distance + 
                    " for " + damage + " damage");
                applyDamage(target, damage);
            }
        } else {
            // Single attack to closest target
            Entity closestTarget = findClosestEntity(defense, validTargets);
            if (closestTarget != null) {
                int distance = CombatRules.calculateDistance(defense, closestTarget);
                log(defense.getEntityName() + " (range:" + defense.getAttackRange() + 
                    ") attacks " + closestTarget.getEntityName() + " at distance " + distance + 
                    " for " + damage + " damage");
                applyDamage(closestTarget, damage);
            }
        }
    }
    
    /**
     * Processes attack for a single zombie
     */
    private void processZombieAttack(Zombie zombie) {
        // Get all valid defense targets in range
        List<Entity> defenseEntities = new ArrayList<>();
        for (Defense d : waveDefense) {
            if (d.getHealthPoints() > 0) {
                defenseEntities.add(d);
            }
        }
        
        // Add life tree as potential target
        if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
            defenseEntities.add(lifeTree);
        }
        
        List<Entity> validTargets = CombatRules.getValidTargets(zombie, defenseEntities);
        
        if (validTargets.isEmpty()) {
            return; // No targets in range, zombie keeps moving
        }
        
        // Check if explosive zombie should explode
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
        
        // Attack closest target
        Entity closestTarget = findClosestEntity(zombie, validTargets);
        if (closestTarget != null) {
            int distance = CombatRules.calculateDistance(zombie, closestTarget);
            log(zombie.getEntityName() + " (range:" + zombie.getAttackRange() + 
                ") attacks " + closestTarget.getEntityName() + " at distance " + distance + 
                " for " + damage + " damage");
            applyDamage(closestTarget, damage);
        }
    }
    
    /**
     * Processes healing for a healer entity
     */
    private void processHealing(Entity healer) {
        // Get all valid heal targets
        List<Entity> candidates = new ArrayList<>();
        
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
        
        List<Entity> validTargets = CombatRules.getValidHealTargets(healer, candidates);
        
        if (validTargets.isEmpty()) {
            return; // No one to heal
        }
        
        // Heal the most damaged ally (healers heal 1 ally at a time)
        Entity mostDamaged = findMostDamagedEntity(validTargets);
        if (mostDamaged != null) {
            int healAmount = getEntityHealPower(healer);
            applyHealing(mostDamaged, healAmount);
        }
    }
    
    /**
     * Explodes a defense, dealing instant kill to all zombies in 3x3 area
     */
    private void explodeDefense(Defense defense, List<Entity> triggeredBy) {
        final int EXPLOSION_RADIUS = 1; // 3x3 area (radius 1 from center)
        
        // Encontrar TODOS los zombies en el área de explosión 3x3
        List<Zombie> zombiesInExplosionArea = new ArrayList<>();
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
        
        // Matar a todos los zombies en el área
        for (Zombie zombie : zombiesInExplosionArea) {
            log("  - Exploding on zombie: " + zombie.getEntityName() + 
                " at (" + zombie.getCurrentRow() + "," + zombie.getCurrentColumn() + ")" +
                " (HP: " + zombie.getHealthPoints() + ", Alive: " + zombie.isAlive() + ")");
            applyDamage(zombie, 999999); // Instant kill
        }
        
        // Defense is destroyed
        defense.setHealthPoints(0);
        removeDefenseFromBoard(defense);
        log("=== EXPLOSION END === " + defense.getEntityName() + " removed from board");
    }
    
    /**
     * Explodes a zombie, dealing instant kill to all defenses in 3x3 area
     */
    private void explodeZombie(Zombie zombie, List<Entity> triggeredBy) {
        final int EXPLOSION_RADIUS = 1; // 3x3 area (radius 1 from center)
        
        // Encontrar TODAS las defensas en el área de explosión 3x3
        List<Defense> defensesInExplosionArea = new ArrayList<>();
        int zombieRow = zombie.getCurrentRow();
        int zombieCol = zombie.getCurrentColumn();
        
        for (Defense d : waveDefense) {
            if (d == null || d.getHealthPoints() <= 0) {
                continue;
            }
            
            int distance = CombatRules.calculateDistance(zombie, d);
            if (distance <= EXPLOSION_RADIUS) {
                defensesInExplosionArea.add(d);
            }
        }
        
        // También incluir Life Tree si está en rango
        if (lifeTree != null && lifeTree.getHealthPoints() > 0) {
            int distance = CombatRules.calculateDistance(zombie, lifeTree);
            if (distance <= EXPLOSION_RADIUS) {
                defensesInExplosionArea.add(lifeTree);
            }
        }
        
        log("=== EXPLOSION === Zombie " + zombie.getEntityName() + 
            " exploded at (" + zombieRow + "," + zombieCol + ")! Defenses in 3x3 area: " + defensesInExplosionArea.size());
        
        // Destruir todas las defensas en el área
        for (Defense defense : defensesInExplosionArea) {
            log("  - Exploding on defense: " + defense.getEntityName() + 
                " at (" + defense.getCurrentRow() + "," + defense.getCurrentColumn() + ")" +
                " (HP: " + defense.getHealthPoints() + ")");
            applyDamage(defense, 999999); // Instant kill
        }
        
        // Zombie dies
        zombie.setHealthPoints(0);
        zombie.setAlive(false);
        registerZombieDefeat(zombie);
        log("=== EXPLOSION END === " + zombie.getEntityName() + " died from explosion");
    }
    
    /**
     * Applies damage to an entity
     */
    private void applyDamage(Entity target, int damage) {
        int currentHealth = target.getHealthPoints();
        int newHealth = Math.max(0, currentHealth - damage);
        target.setHealthPoints(newHealth);
        
        log(target.getEntityName() + " took " + damage + " damage! HP: " + currentHealth + " -> " + newHealth);
        
        if (newHealth <= 0) {
            log(target.getEntityName() + " has been defeated!");
            
            if (target instanceof Zombie) {
                Zombie z = (Zombie) target;
                z.setAlive(false);
                registerZombieDefeat(z);
            } else if (target instanceof Defense) {
                Defense d = (Defense) target;
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
    private Entity findClosestEntity(Entity source, List<Entity> candidates) {
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
     * Finds the most damaged entity from a list (for healing priority)
     */
    private Entity findMostDamagedEntity(List<Entity> candidates) {
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
        List<Zombie> zombiesToRemove = new ArrayList<>();
        
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
            }
        }
        
        if (!zombiesToRemove.isEmpty()) {
            waveZombies.removeAll(zombiesToRemove);
            log("Removed " + zombiesToRemove.size() + " dead zombies from wave list");
        }
        
        // Remove dead defenses
        List<Defense> defensesToRemove = new ArrayList<>();
        
        for (Defense defense : waveDefense) {
            if (defense == null || defense.getHealthPoints() <= 0) {
                defensesToRemove.add(defense);
            }
        }
        
        if (!defensesToRemove.isEmpty()) {
            waveDefense.removeAll(defensesToRemove);
            log("Removed " + defensesToRemove.size() + " dead defenses from wave list");
        }
    }
}
