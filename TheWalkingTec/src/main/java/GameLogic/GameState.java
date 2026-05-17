package GameLogic;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class GameState implements Serializable {

    public static final String PROPERTY_LEVEL = "level";
    public static final String PROPERTY_HEALTH = "baseHealth";
    public static final String PROPERTY_COINS = "coins";
    public static final String PROPERTY_PAUSED = "isPaused";
    public static final String PROPERTY_ROUND_ACTIVE = "roundActive";
    public static final String PROPERTY_WAVE_GENERATED = "waveGenerated";
    public static final String PROPERTY_ZOMBIES_REMAINING = "zombiesRemaining";
    public static final String PROPERTY_DEFENSE_LIMIT = "defenseCostLimit";
    public static final String PROPERTY_DEFENSE_USED = "defenseCostUsed";

    private final PropertyChangeSupport support;

    private volatile int level = 1;
    private volatile int baseHealth = 100;
    private volatile int coinsThisLevel = 0;
    private volatile int defenseCostLimit = 0;
    private volatile int defenseCostUsed = 0;

    private volatile boolean isPaused = true;
    private volatile boolean roundActive = false;
    private volatile boolean waveGenerated = false;

    private volatile int zombiesRemaining = 0;
    private volatile int totalZombiesInWave = 0;

    private volatile boolean victoryProcessed = false;
    private volatile boolean lossProcessed = false;
    private volatile boolean summaryShown = false;

    private volatile int nextZombieIndexToSpawn = 0;
    private volatile int lifeTreeRow = -1;
    private volatile int lifeTreeColumn = -1;
    private volatile int lifeTreeInitialHealth = 100;

    public GameState() {
        this.support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public int getLevel() { return level; }

    public void setLevel(int level) {
        int old = this.level;
        this.level = level;
        support.firePropertyChange(PROPERTY_LEVEL, old, level);
    }

    public int getBaseHealth() { return baseHealth; }

    public void setBaseHealth(int baseHealth) {
        int old = this.baseHealth;
        this.baseHealth = baseHealth;
        support.firePropertyChange(PROPERTY_HEALTH, old, baseHealth);
    }

    public int getCoinsThisLevel() { return coinsThisLevel; }

    public void setCoinsThisLevel(int coins) {
        int old = this.coinsThisLevel;
        this.coinsThisLevel = coins;
        support.firePropertyChange(PROPERTY_COINS, old, coins);
    }

    public boolean isPaused() { return isPaused; }

    public void setPaused(boolean paused) {
        boolean old = this.isPaused;
        this.isPaused = paused;
        support.firePropertyChange(PROPERTY_PAUSED, old, paused);
    }

    public boolean isRoundActive() { return roundActive; }

    public void setRoundActive(boolean roundActive) {
        boolean old = this.roundActive;
        this.roundActive = roundActive;
        support.firePropertyChange(PROPERTY_ROUND_ACTIVE, old, roundActive);
    }

    public boolean isWaveGenerated() { return waveGenerated; }

    public void setWaveGenerated(boolean waveGenerated) {
        boolean old = this.waveGenerated;
        this.waveGenerated = waveGenerated;
        support.firePropertyChange(PROPERTY_WAVE_GENERATED, old, waveGenerated);
    }

    public int getZombiesRemaining() { return zombiesRemaining; }

    public void setZombiesRemaining(int count) {
        int old = this.zombiesRemaining;
        this.zombiesRemaining = count;
        support.firePropertyChange(PROPERTY_ZOMBIES_REMAINING, old, count);
    }

    public int getDefenseCostLimit() { return defenseCostLimit; }

    public void setDefenseCostLimit(int limit) {
        int old = this.defenseCostLimit;
        this.defenseCostLimit = limit;
        support.firePropertyChange(PROPERTY_DEFENSE_LIMIT, old, limit);
    }

    public int getDefenseCostUsed() { return defenseCostUsed; }

    public void setDefenseCostUsed(int used) {
        int old = this.defenseCostUsed;
        this.defenseCostUsed = used;
        support.firePropertyChange(PROPERTY_DEFENSE_USED, old, used);
    }

    public int getTotalZombiesInWave() { return totalZombiesInWave; }
    public void setTotalZombiesInWave(int total) { this.totalZombiesInWave = total; }

    public boolean isVictoryProcessed() { return victoryProcessed; }
    public void setVictoryProcessed(boolean processed) { this.victoryProcessed = processed; }

    public boolean isLossProcessed() { return lossProcessed; }
    public void setLossProcessed(boolean processed) { this.lossProcessed = processed; }

    public boolean isSummaryShown() { return summaryShown; }
    public void setSummaryShown(boolean shown) { this.summaryShown = shown; }

    public int getNextZombieIndexToSpawn() { return nextZombieIndexToSpawn; }
    public void setNextZombieIndexToSpawn(int index) { this.nextZombieIndexToSpawn = index; }

    public int getLifeTreeRow() { return lifeTreeRow; }
    public void setLifeTreeRow(int row) { this.lifeTreeRow = row; }

    public int getLifeTreeColumn() { return lifeTreeColumn; }
    public void setLifeTreeColumn(int col) { this.lifeTreeColumn = col; }

    public int getLifeTreeInitialHealth() { return lifeTreeInitialHealth; }
    public void setLifeTreeInitialHealth(int health) { this.lifeTreeInitialHealth = health; }

    public void reset() {
        setLevel(1);
        setBaseHealth(100);
        setCoinsThisLevel(0);
        setPaused(true);
        setRoundActive(false);
        setWaveGenerated(false);
        setZombiesRemaining(0);
        setDefenseCostLimit(0);
        setDefenseCostUsed(0);
        this.totalZombiesInWave = 0;
        this.victoryProcessed = false;
        this.lossProcessed = false;
        this.summaryShown = false;
        this.nextZombieIndexToSpawn = 0;
        this.lifeTreeRow = -1;
        this.lifeTreeColumn = -1;
        this.lifeTreeInitialHealth = 100;
    }

    public void resetForNewRound() {
        setPaused(true);
        setRoundActive(false);
        setWaveGenerated(false);
        setZombiesRemaining(0);
        this.victoryProcessed = false;
        this.lossProcessed = false;
        this.summaryShown = false;
        this.nextZombieIndexToSpawn = 0;
    }
}
