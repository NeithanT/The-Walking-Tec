package GameLogic;

import Configuration.ConfigManager;
import Defense.Defense;
import Defense.DefenseAttacker;
import Defense.DefenseHealer;
import Entity.Entity;
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

    private final Lock summaryLock = new ReentrantLock();

    private final GameState state;
    private final GameBoard board;
    private final SidePanel sidePanel;
    private final MatrixManager matrixManager;
    private final ConfigManager configMg;
    private final Random rnd;
    private final WaveOrchestrator waveOrchestrator;
    private final ZombieMovementController movementController;
    private final CombatManager combatManager;
    private final PlacementManager placementManager;
    private final ArrayList<Zombie> waveZombies;
    private final ArrayList<Defense> waveDefense;

    private CombatLog combatLog;
    private javax.swing.JFrame parentFrame;

    private Timer gameTimer;
    private Timer combatTimer;
    private volatile Defense lifeTree;
    private volatile boolean isPaused;

    public GameManager(GameBoard board, SidePanel sidePanel) {
        this.state = new GameState();
        this.board = board;
        this.sidePanel = sidePanel;
        this.parentFrame = null;
        this.matrixManager = new MatrixManager();
        this.matrixManager.setSidePanel(sidePanel);
        this.configMg = new ConfigManager();
        this.rnd = new Random();
        this.waveZombies = new ArrayList<>();
        this.waveDefense = new ArrayList<>();
        this.movementController = new ZombieMovementController(this);
        this.waveOrchestrator = new WaveOrchestrator(state, board, sidePanel, configMg, rnd, waveZombies, waveDefense);
        this.waveOrchestrator.setGameManager(this);
        this.combatManager = new CombatManager(state, board, waveZombies, waveDefense);
        this.placementManager = new PlacementManager(state, board, sidePanel, matrixManager, rnd, waveDefense);

        this.isPaused = true;
        this.lifeTree = null;

        setupCombatManagerCallbacks();
    }

    private void setupCombatManagerCallbacks() {
        combatManager.setOnEntityChanged(() -> {
            if (sidePanel != null) sidePanel.refreshStatusCounters();
        });
        combatManager.setOnVictoryCheck(this::verifyVictory);
        combatManager.setOnLossCheck(this::verifyLoss);
    }

    public boolean startGame() {
        if (gameTimer != null && gameTimer.isRunning()) {
            log("Game is already running!");
            return false;
        }

        if (!placementManager.isLifeTreePlaced()) {
            log("===========================================");
            log("  ERROR: You must place the Life Tree     ");
            log("         before starting the game!        ");
            log("===========================================");
            return false;
        }

        if (gameTimer == null) {
            gameTimer = new Timer(17, e -> update());
        }
        if (!gameTimer.isRunning()) gameTimer.start();

        if (combatTimer != null) combatTimer.stop();
        combatTimer = new Timer(1000, e -> processCombat());
        combatTimer.start();

        isPaused = false;
        state.setPaused(false);

        if (!state.isRoundActive()) startRound();

        log("Juego iniciado");
        return true;
    }

    public void pauseGame() {
        isPaused = !isPaused;
        state.setPaused(isPaused);
        log(isPaused ? "Juego Pausado" : "Juego reanudado");
    }

    public void stopGame() {
        if (gameTimer != null) gameTimer.stop();
        if (combatTimer != null) combatTimer.stop();
        isPaused = true;
        state.setPaused(true);
        waveOrchestrator.stopAllZombies();
        stopDefenseThreads();
    }

    private void stopDefenseThreads() {
        for (Defense d : waveDefense) {
            if (d != null) d.stopThread();
        }
    }

    private void startDefenseThreads() {
        for (Defense d : waveDefense) {
            if (d != null && d.getHealthPoints() > 0) {
                d.setGameManager(this);
                d.startThread();
            }
        }
    }

    public void setSelectedDefense(Defense defense) {
        placementManager.setSelectedDefense(defense);
        log("Selected Defense: " + (defense != null ? defense.getEntityName() : "null"));
    }

    public boolean sellDefenseAt(int row, int column) {
        return placementManager.sellDefenseAt(row, column);
    }

    public boolean placeDefences(int row, int column) {
        return placementManager.placeDefences(row, column);
    }

    public boolean isValidPlacement(int row, int column) {
        return placementManager.isValidPlacement(row, column);
    }

    public Zombie findClosestZombieInRange(Defense defense) {
        return combatManager.findClosestZombieInRange(defense);
    }

    public void processDefenseAttackThreaded(Defense defense) {
        combatManager.processDefenseAttackThreaded(defense);
    }

    public void processZombieAttackThreaded(Zombie zombie) {
        combatManager.processZombieAttackThreaded(zombie);
    }

    public void update() {
        if (isPaused) return;
        board.repaint();
        verifyVictory();
        verifyLoss();
    }

    private void processCombat() {
        if (isPaused || !state.isRoundActive()) return;
        combatManager.processCombat();
    }

    public void moveZombieTowardsLifeTree(Zombie zombie) {
        movementController.moveZombieTowardsLifeTree(zombie);
    }

    public void moveZombieTowardsClosestTarget(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) return;
        Defense closest = combatManager.findClosestDefense(zombie);
        if (closest != null) {
            movementController.moveZombieTowardsTarget(zombie,
                closest.getCurrentRow(), closest.getCurrentColumn());
        } else {
            movementController.moveZombieTowardsLifeTree(zombie);
        }
    }

    public int calculateDistanceBetween(Entity e1, Entity e2) {
        if (e1 == null || e2 == null) return Integer.MAX_VALUE;
        return CombatRules.calculateDistance(e1, e2);
    }

    void zombieReachedLifeTree(Zombie zombie) {
        if (zombie == null) return;
        if (lifeTree == null || lifeTree.getHealthPoints() <= 0) {
            zombie.setAlive(false);
            return;
        }

        int damage = Math.max(1, zombie.getHealthPoints() / 10);
        state.setBaseHealth(state.getBaseHealth() - damage);

        int newHealth = Math.max(0, lifeTree.getHealthPoints() - damage);
        lifeTree.setHealthPoints(newHealth);

        zombie.setAlive(false);

        if (combatLog != null) combatLog.logDeath(zombie, lifeTree);
        registerZombieDefeat(zombie);

        log("Zombie attacked the Life Tree! Damage: " + damage
            + " | Life Tree health: " + lifeTree.getHealthPoints()
            + " | Base health: " + state.getBaseHealth());

        if (lifeTree.getHealthPoints() <= 0) {
            log("The Life Tree has been destroyed!");
            destroyLifeTree();
        }

        if (sidePanel != null) sidePanel.refreshStatusCounters();
        board.repaint();
    }

    private void destroyLifeTree() {
        if (lifeTree != null) {
            board.deleteDefense(new PlacedDefense(lifeTree, state.getLifeTreeRow(), state.getLifeTreeColumn(), null));
        }
        state.setBaseHealth(0);
        if (lifeTree != null) lifeTree.setHealthPoints(0);
        waveOrchestrator.stopAllZombies();
        state.setLifeTreeRow(-1);
        state.setLifeTreeColumn(-1);
        verifyLoss();
    }

    public boolean isThereSpaceLeft(int totalSpace) {
        return state.getDefenseCostUsed() < totalSpace;
    }

    public void verifyVictory() {
        if (!state.isWaveGenerated() || state.isVictoryProcessed()) return;

        int alive = 0;
        int unspawned = 0;
        for (Zombie z : waveZombies) {
            if (z != null && z.isAlive() && z.getHealthPoints() > 0) {
                if (z.getState() == Thread.State.NEW) unspawned++;
                else alive++;
            }
        }

        if (alive == 0 && unspawned == 0) {
            log("===========================================");
            log("         VICTORY - LEVEL COMPLETE!        ");
            log("   All zombies have been defeated!       ");
            log("===========================================");

            state.setVictoryProcessed(true);

            if (combatLog != null) {
                combatLog.markRemainingEntitiesDead(true);
                combatLog.endBattle();
            }

            stopGame();
            showBattleSummary(true);
        }
    }

    public void verifyLoss() {
        if (state.isLossProcessed()) return;

        if (state.getBaseHealth() <= 0 || (lifeTree != null && lifeTree.getHealthPoints() <= 0)) {
            log("===========================================");
            log("         GAME OVER - YOU LOST!            ");
            log("   The Life Tree has been destroyed!      ");
            log("===========================================");

            state.setLossProcessed(true);

            if (combatLog != null) {
                combatLog.markRemainingEntitiesDead(false);
                combatLog.endBattle();
            }

            stopGame();
            showBattleSummary(false);
        }
    }

    private void showBattleSummary(boolean hasWon) {
        summaryLock.lock();
        try {
            if (state.isSummaryShown()) {
                log("Summary already shown, skipping duplicate call");
                return;
            }
            state.setSummaryShown(true);
        } finally {
            summaryLock.unlock();
        }

        if (parentFrame == null || combatLog == null) {
            if (hasWon) showVictoryDialogDirect();
            else showGameOverDialogDirect(false);
            return;
        }

        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> showBattleSummaryInternal(hasWon));
        } else {
            showBattleSummaryInternal(hasWon);
        }
    }

    private void showBattleSummaryInternal(boolean hasWon) {
        Table.BattleSummaryDialog.showSummary(parentFrame, combatLog, hasWon);
        if (hasWon) showVictoryDialogDirect();
        else showGameOverDialogDirect(false);
    }

    private void showVictoryDialogDirect() {
        if (parentFrame == null) { advanceToNextRound(); return; }
        Table.GameOverDialog.PlayerChoice choice =
            Table.GameOverDialog.showGameOverDialog(parentFrame, true);
        handleVictoryChoice(choice);
    }

    private void handleVictoryChoice(Table.GameOverDialog.PlayerChoice choice) {
        switch (choice) {
            case RETRY_LEVEL -> retryLevel();
            case NEXT_LEVEL -> advanceToNextRound();
            case RETURN_TO_MENU -> { resetGame(); if (sidePanel != null) sidePanel.returnToMenu(); }
            default -> advanceToNextRound();
        }
    }

    private void showGameOverDialogDirect(boolean hasWon) {
        if (parentFrame == null) { resetGame(); return; }
        Table.GameOverDialog.PlayerChoice choice =
            Table.GameOverDialog.showGameOverDialog(parentFrame, hasWon);
        handlePlayerChoice(choice);
    }

    private void handlePlayerChoice(Table.GameOverDialog.PlayerChoice choice) {
        switch (choice) {
            case RETRY_LEVEL -> retryLevel();
            case NEXT_LEVEL -> { resetGame(); advanceToNextRound(); }
            case RETURN_TO_MENU -> { resetGame(); if (sidePanel != null) sidePanel.returnToMenu(); }
            default -> resetGame();
        }
    }

    private void advanceToNextRound() {
        stopGame();

        state.setLevel(state.getLevel() + 1);
        int coins = WaveOrchestrator.coinsForLevel(state.getLevel());
        state.setCoinsThisLevel(coins);
        state.setDefenseCostLimit(coins);
        state.setDefenseCostUsed(0);

        if (board != null) {
            board.clearZombies();
            board.clearDefenses();
            board.clearSelectedDefense();
        }

        waveZombies.clear();
        waveDefense.clear();
        matrixManager.restartMatrix();

        state.resetForNewRound();

        if (lifeTree != null) {
            int initial = state.getLifeTreeInitialHealth();
            lifeTree.setHealthPoints(initial);
            state.setBaseHealth(initial);
        }

        placementManager.setLifeTree(null);
        state.setLifeTreeRow(-1);
        state.setLifeTreeColumn(-1);

        if (sidePanel != null) {
            sidePanel.showLifeTreeInCatalog();
            sidePanel.enableStartButton();
            sidePanel.updateAllLabels();
        }

        log("===========================================");
        log("         LEVEL " + state.getLevel() + " STARTING...        ");
        log("   Coins available: " + coins);
        log("   Place your defenses and Life Tree!      ");
        log("===========================================");
    }

    private void retryLevel() {
        log("===========================================");
        log("         RETRYING LEVEL...                ");
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

        lifeTree = null;
        state.setLifeTreeRow(-1);
        state.setLifeTreeColumn(-1);
        state.setBaseHealth(100);

        state.setDefenseCostUsed(0);
        placementManager.setSelectedDefense(null);

        state.resetForNewRound();

        int coins = WaveOrchestrator.coinsForLevel(state.getLevel());
        state.setCoinsThisLevel(coins);
        state.setDefenseCostLimit(coins);

        if (sidePanel != null) {
            sidePanel.updateAllLabels();
            sidePanel.enableStartButton();
            sidePanel.showLifeTreeInCatalog();
        }

        log("Level " + state.getLevel() + " ready to retry. Place your defenses again!");
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

        state.reset();

        lifeTree = null;
        state.setLifeTreeRow(-1);
        state.setLifeTreeColumn(-1);

        int coins = WaveOrchestrator.coinsForLevel(1);
        state.setCoinsThisLevel(coins);
        state.setDefenseCostLimit(coins);

        placementManager.setSelectedDefense(null);
        combatLog = null;

        if (sidePanel != null) {
            sidePanel.showLifeTreeInCatalog();
            sidePanel.updateAllLabels();
            sidePanel.enableStartButton();
        }

        log("Game reset complete. Place the Life Tree to start again.");
    }

    public void registerZombieDefeat(Zombie zombie) {
        if (zombie == null) {
            log("WARNING: registerZombieDefeat called with null zombie");
            return;
        }
        if (!zombie.isAlive()) {
            log("WARNING: registerZombieDefeat called for already dead zombie: " + zombie.getEntityName());
            return;
        }

        zombie.setAlive(false);
        zombie.setHealthPoints(0);
        board.deleteZombie(zombie);

        log("Zombie defeated: " + zombie.getEntityName() +
            " | Remaining: " + getZombiesRemaining());

        if (sidePanel != null) sidePanel.refreshStatusCounters();
        verifyVictory();
    }

    private void startRound() {
        if (placementManager.getSelectedDefense() != null) {
            placementManager.setSelectedDefense(null);
            board.clearSelectedDefense();
            if (sidePanel != null) sidePanel.deselectDefense();
        }

        startDefenseThreads();
        waveOrchestrator.startRound();
    }

    public void initializeCombatLog() {
        this.combatLog = new CombatLog(state.getLevel());
        combatManager.setCombatLog(combatLog);
        log("=== Combat Log Initialized for Level " + state.getLevel() + " ===");

        int count = 0;
        for (Defense d : waveDefense) {
            if (d != null && combatLog != null) {
                combatLog.getStats(d);
                count++;
            }
        }
        log("Combat log: Registered " + count + " defenses");
    }

    public void registerZombiesInCombatLog() {
        if (combatLog == null) return;
        int count = 0;
        for (Zombie z : waveZombies) {
            if (z != null) { combatLog.getStats(z); count++; }
        }
        log("Combat log: Registered " + count + " zombies");
    }

    public void setLifeTree(Defense tree) {
        this.lifeTree = tree;
        combatManager.setLifeTree(tree);
    }

    // ==================== ACCESSORS ====================

    public boolean isGamePaused() { return isPaused; }

    public int getLevel() { return state.getLevel(); }
    public void setLevel(int level) {
        state.setLevel(level);
        state.setCoinsThisLevel(WaveOrchestrator.coinsForLevel(level));
        state.setDefenseCostLimit(state.getCoinsThisLevel());
        this.combatLog = new CombatLog(level);
        combatManager.setCombatLog(combatLog);
        if (sidePanel != null) sidePanel.updateAllLabels();
        log("Level set to " + level + " - Coins: " + state.getCoinsThisLevel());
    }

    public int getBaseHealth() { return state.getBaseHealth(); }
    public Defense getLifeTreeEntity() { return lifeTree; }
    public boolean isLifeTreePlaced() { return placementManager.isLifeTreePlaced(); }
    public int getLifeTreeHealth() { return lifeTree != null ? lifeTree.getHealthPoints() : 0; }
    public int getLifeTreeRow() { return state.getLifeTreeRow(); }
    public int getLifeTreeColumn() { return state.getLifeTreeColumn(); }
    public Defense getSelectedDefense() { return placementManager.getSelectedDefense(); }
    public int getCoinsThisLevel() { return state.getCoinsThisLevel(); }
    public void setCoinsThisLevel(int coins) { state.setCoinsThisLevel(coins); }
    public int getDefenseCostLimit() { return state.getDefenseCostLimit(); }
    public void setDefenseCostLimit(int limit) { state.setDefenseCostLimit(limit); }
    public int getDefenseCostUsed() { return state.getDefenseCostUsed(); }
    public void setDefenseCostUsed(int used) { state.setDefenseCostUsed(used); }

    public int getZombiesRemaining() {
        int count = 0;
        for (Zombie z : waveZombies) {
            if (z != null && z.isAlive() && z.getHealthPoints() > 0) count++;
        }
        return count;
    }

    public void setZombiesRemaining(int n) { state.setZombiesRemaining(n); }
    public int getTotalZombiesInWave() { return state.getTotalZombiesInWave(); }
    public void setTotalZombiesInWave(int n) { state.setTotalZombiesInWave(n); }

    public boolean isRoundActive() { return state.isRoundActive(); }
    public void setRoundActive(boolean active) { state.setRoundActive(active); }
    public boolean isWaveGenerated() { return state.isWaveGenerated(); }
    public void setWaveGenerated(boolean gen) { state.setWaveGenerated(gen); }

    public ArrayList<Zombie> getWaveZombiesInternal() { return waveZombies; }
    public ArrayList<Defense> getWaveDefenseInternal() { return waveDefense; }

    public GameBoard getBoard() { return board; }
    public SidePanel getSidePanel() { return sidePanel; }
    public ConfigManager getConfigManager() { return configMg; }
    public Random getRandomGenerator() { return rnd; }
    public CombatLog getCombatLog() { return combatLog; }
    public MatrixManager getMatrixManager() { return matrixManager; }
    public GameState getGameState() { return state; }

    public void setParentFrame(javax.swing.JFrame frame) { this.parentFrame = frame; }

    private void log(String message) {
        if (sidePanel != null) sidePanel.appendLog(message);
    }
}
