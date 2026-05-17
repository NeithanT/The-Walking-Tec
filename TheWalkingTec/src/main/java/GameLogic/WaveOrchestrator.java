package GameLogic;

import Configuration.ConfigManager;
import Defense.Defense;
import Table.GameBoard;
import Table.PlacedDefense;
import Table.SidePanel;
import Zombie.Zombie;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;

public class WaveOrchestrator {

    private static final int ZOMBIES_PER_SPAWN_BATCH = 5;
    private static final int SPAWN_DELAY_MS = 2000;

    private final GameState state;
    private final GameBoard board;
    private final SidePanel sidePanel;
    private final ConfigManager configMg;
    private final Random rnd;
    private final ArrayList<Zombie> waveZombies;
    private final ArrayList<Defense> waveDefense;

    private GameManager gameManager;
    private Timer zombieSpawnTimer;

    public void setGameManager(GameManager gm) { this.gameManager = gm; }

    public WaveOrchestrator(GameState state, GameBoard board, SidePanel sidePanel,
                            ConfigManager configMg, Random rnd,
                            ArrayList<Zombie> waveZombies,
                            ArrayList<Defense> waveDefense) {
        this.state = state;
        this.board = board;
        this.sidePanel = sidePanel;
        this.configMg = configMg;
        this.rnd = rnd;
        this.waveZombies = waveZombies;
        this.waveDefense = waveDefense;
    }

    static int coinsForLevel(int level) { return 20 + 5 * (level - 1); }
    static int zombiesForLevel(int level) { return 20 + 5 * (level - 1); }

    public void startRound() {
        state.setWaveGenerated(false);
        state.setRoundActive(false);

        int coins = coinsForLevel(state.getLevel());
        state.setCoinsThisLevel(coins);
        state.setDefenseCostLimit(coins);

        waveZombies.clear();
        syncWaveDefenseWithBoard();
        generateWave();

        state.setRoundActive(true);
        if (sidePanel != null) sidePanel.refreshStatusCounters();
    }

    private void syncWaveDefenseWithBoard() {
        waveDefense.clear();
        if (board == null) return;
        for (PlacedDefense pd : board.getDefenses()) {
            if (pd != null && pd.definition != null) {
                waveDefense.add(pd.definition);
            }
        }
    }

    private void generateWave() {
        if (state.isWaveGenerated()) return;

        ArrayList<Zombie> pool = configMg.getZombies();
        if (pool == null || pool.isEmpty()) {
            log("No zombies configured. Cannot generate wave.");
            state.setWaveGenerated(true);
            return;
        }

        int level = state.getLevel();
        ArrayList<Zombie> available = new ArrayList<>();
        for (Zombie z : pool) {
            if (z != null && z.getShowUpLevel() <= level) available.add(z);
        }
        if (available.isEmpty()) {
            for (Zombie z : pool) { if (z != null) available.add(z); }
        }
        if (available.isEmpty()) {
            log("No valid zombies found for current round.");
            state.setWaveGenerated(true);
            return;
        }

        int target = zombiesForLevel(level);
        waveZombies.clear();
        if (board != null) board.clearZombies();

        for (int i = 0; i < target; i++) {
            Zombie proto = available.get(rnd.nextInt(available.size()));
            Zombie spawned = EntityFactory.cloneZombie(proto);
            if (spawned == null) continue;

            EntityFactory.applyZombieScaling(spawned, level, rnd);
            spawned.setAlive(true);
            if (gameManager != null) spawned.setGameManager(gameManager);

            waveZombies.add(spawned);
            if (board != null) board.addZombie(spawned);

            String name = spawned.getEntityName() != null ? spawned.getEntityName() : "Zombie";
            log("Spawn zombie: " + name + " (#" + (i + 1) + ")");
        }

        state.setZombiesRemaining(waveZombies.size());
        state.setTotalZombiesInWave(waveZombies.size());
        state.setWaveGenerated(true);

        log("Wave level [" + level + "] generated with " + waveZombies.size() + " zombies (target: " + target + ")");

        startZombieSpawn();

        if (sidePanel != null) sidePanel.refreshStatusCounters();
    }

    public void startZombieSpawn() {
        state.setNextZombieIndexToSpawn(0);
        log("Starting zombie spawn system. Total zombies: " + waveZombies.size());

        if (zombieSpawnTimer != null && zombieSpawnTimer.isRunning()) {
            zombieSpawnTimer.stop();
        }

        spawnNextBatch();

        if (state.getNextZombieIndexToSpawn() < waveZombies.size()) {
            zombieSpawnTimer = new Timer(SPAWN_DELAY_MS, e -> {
                if (!state.isPaused() && state.isRoundActive()) {
                    spawnNextBatch();
                    if (state.getNextZombieIndexToSpawn() >= waveZombies.size()) {
                        ((Timer) e.getSource()).stop();
                        log("All zombies spawned! Timer stopped.");
                    }
                }
            });
            zombieSpawnTimer.setRepeats(true);
            zombieSpawnTimer.start();
            log("Zombie spawn timer started with " + SPAWN_DELAY_MS + "ms delay");
        }
    }

    public void stopZombieSpawn() {
        if (zombieSpawnTimer != null && zombieSpawnTimer.isRunning()) {
            zombieSpawnTimer.stop();
        }
    }

    public void stopAllZombies() {
        stopZombieSpawn();
        for (Zombie z : waveZombies) {
            if (z != null) {
                z.setAlive(false);
                z.stopThread();
            }
        }
    }

    private void spawnNextBatch() {
        if (waveZombies.isEmpty()) {
            log("WARNING: waveZombies is empty, cannot spawn");
            stopZombieSpawn();
            return;
        }

        int idx = state.getNextZombieIndexToSpawn();
        if (idx >= waveZombies.size()) {
            log("WARNING: All zombies already spawned (" + idx + "/" + waveZombies.size() + ")");
            stopZombieSpawn();
            return;
        }

        int end = Math.min(idx + ZOMBIES_PER_SPAWN_BATCH, waveZombies.size());
        log("Spawning batch: indices " + idx + " to " + (end - 1));

        for (int i = idx; i < end; i++) {
            Zombie z = waveZombies.get(i);
            if (z != null) {
                z.setAlive(true);
                if (z.getState() == Thread.State.NEW) {
                    z.start();
                }
            }
        }

        state.setNextZombieIndexToSpawn(end);
        log("Spawned batch (" + end + "/" + waveZombies.size() + ")");

        if (sidePanel != null) sidePanel.refreshStatusCounters();
    }

    private void log(String msg) {
        if (sidePanel != null) sidePanel.appendLog(msg);
    }
}
