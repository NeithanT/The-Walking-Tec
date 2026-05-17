package GameLogic;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * GameState - Central repository for all reactive game variables.
 * Uses PropertyChangeSupport to notify observers (UI, Managers) of changes.
 */
public class GameState implements Serializable {
    
    // Property Names for Observers
    public static final String PROPERTY_LEVEL = "level";
    public static final String PROPERTY_HEALTH = "baseHealth";
    public static final String PROPERTY_COINS = "coins";
    public static final String PROPERTY_PAUSED = "isPaused";
    public static final String PROPERTY_ROUND_ACTIVE = "roundActive";
    public static final String PROPERTY_WAVE_GENERATED = "waveGenerated";
    public static final String PROPERTY_ZOMBIES_REMAINING = "zombiesRemaining";
    
    private final PropertyChangeSupport support;
    
    // Game State Variables
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

    public GameState() {
        this.support = new PropertyChangeSupport(this);
    }

    // --- Observer Pattern Methods ---
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // --- Getters and Setters with Property Notifications ---

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        int old = this.level;
        this.level = level;
        support.firePropertyChange(PROPERTY_LEVEL, old, level);
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public void setBaseHealth(int baseHealth) {
        int old = this.baseHealth;
        this.baseHealth = baseHealth;
        support.firePropertyChange(PROPERTY_HEALTH, old, baseHealth);
    }

    public int getCoinsThisLevel() {
        return coinsThisLevel;
    }

    public void setCoinsThisLevel(int coins) {
        int old = this.coinsThisLevel;
        this.coinsThisLevel = coins;
        support.firePropertyChange(PROPERTY_COINS, old, coins);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        boolean old = this.isPaused;
        this.isPaused = paused;
        support.firePropertyChange(PROPERTY_PAUSED, old, paused);
    }

    public boolean isRoundActive() {
        return roundActive;
    }

    public void setRoundActive(boolean roundActive) {
        boolean old = this.roundActive;
        this.roundActive = roundActive;
        support.firePropertyChange(PROPERTY_ROUND_ACTIVE, old, roundActive);
    }

    public boolean isWaveGenerated() {
        return waveGenerated;
    }

    public void setWaveGenerated(boolean waveGenerated) {
        boolean old = this.waveGenerated;
        this.waveGenerated = waveGenerated;
        support.firePropertyChange(PROPERTY_WAVE_GENERATED, old, waveGenerated);
    }

    public int getZombiesRemaining() {
        return zombiesRemaining;
    }

    public void setZombiesRemaining(int count) {
        int old = this.zombiesRemaining;
        this.zombiesRemaining = count;
        support.firePropertyChange(PROPERTY_ZOMBIES_REMAINING, old, count);
    }

    // --- Standard Getters/Setters for non-notifying state ---

    public int getDefenseCostLimit() { return defenseCostLimit; }
    public void setDefenseCostLimit(int limit) { this.defenseCostLimit = limit; }

    public int getDefenseCostUsed() { return defenseCostUsed; }
    public void setDefenseCostUsed(int used) { this.defenseCostUsed = used; }

    public int getTotalZombiesInWave() { return totalZombiesInWave; }
    public void setTotalZombiesInWave(int total) { this.totalZombiesInWave = total; }

    public boolean isVictoryProcessed() { return victoryProcessed; }
    public void setVictoryProcessed(boolean processed) { this.victoryProcessed = processed; }

    public boolean isLossProcessed() { return lossProcessed; }
    public void setLossProcessed(boolean processed) { this.lossProcessed = processed; }

    public boolean isSummaryShown() { return summaryShown; }
    public void setSummaryShown(boolean shown) { this.summaryShown = shown; }
    
    /**
     * Resets the state for a new game
     */
    public void reset() {
        setLevel(1);
        setBaseHealth(100);
        setPaused(true);
        setRoundActive(false);
        setWaveGenerated(false);
        setZombiesRemaining(0);
        this.totalZombiesInWave = 0;
        this.defenseCostUsed = 0;
        this.victoryProcessed = false;
        this.lossProcessed = false;
        this.summaryShown = false;
    }
}
