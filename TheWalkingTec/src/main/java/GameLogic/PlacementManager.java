package GameLogic;

import Defense.Defense;
import Defense.DefenseType;
import Table.GameBoard;
import Table.PlacedDefense;
import Table.SidePanel;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class PlacementManager {

    private static final String LIFE_TREE_NAME = "LIFE TREE";

    private final GameState state;
    private final GameBoard board;
    private final SidePanel sidePanel;
    private final MatrixManager matrixManager;
    private final Random rnd;
    private final ArrayList<Defense> waveDefense;

    private Defense selectedDefense;
    private Defense lifeTree;
    private PlacedDefense lifeTreePlaced;

    public PlacementManager(GameState state, GameBoard board, SidePanel sidePanel,
                            MatrixManager matrixManager, Random rnd,
                            ArrayList<Defense> waveDefense) {
        this.state = state;
        this.board = board;
        this.sidePanel = sidePanel;
        this.matrixManager = matrixManager;
        this.rnd = rnd;
        this.waveDefense = waveDefense;
    }

    public void setSelectedDefense(Defense def) {
        this.selectedDefense = def;
        board.setSelectedDefense(def != null ? def.getEntityName() : null);
    }

    public Defense getSelectedDefense() { return selectedDefense; }
    public Defense getLifeTree() { return lifeTree; }
    public PlacedDefense getLifeTreePlaced() { return lifeTreePlaced; }
    public int getLifeTreeRow() { return state.getLifeTreeRow(); }
    public int getLifeTreeColumn() { return state.getLifeTreeColumn(); }
    public int getLifeTreeInitialHealth() { return state.getLifeTreeInitialHealth(); }

    public void setLifeTree(Defense tree) { this.lifeTree = tree; }

    public boolean sellDefenseAt(int row, int column) {
        if (state.isRoundActive()) {
            log("Cannot sell defenses while a round is in progress!");
            return false;
        }

        PlacedDefense placed = board.getDefenseAt(row, column);
        if (placed == null || placed.definition == null) {
            log("No defense at this position to sell");
            return false;
        }

        Defense defense = placed.definition;
        String name = defense.getEntityName();
        boolean isLifeTree = LIFE_TREE_NAME.equalsIgnoreCase(name);

        board.removePlacedDefense(row, column);
        matrixManager.free(row, column);

        if (!isLifeTree) {
            waveDefense.remove(defense);
            int refund = Math.max(1, defense.getCost());
            state.setDefenseCostUsed(Math.max(0, state.getDefenseCostUsed() - refund));
            log("Sold " + name + " for " + refund + " coins");
        } else {
            lifeTree = null;
            lifeTreePlaced = null;
            state.setLifeTreeRow(-1);
            state.setLifeTreeColumn(-1);
            state.setBaseHealth(100);
            log("Life Tree returned to catalog");
            if (sidePanel != null) sidePanel.showLifeTreeInCatalog();
        }

        if (sidePanel != null) sidePanel.refreshStatusCounters();
        board.repaint();
        return true;
    }

    public boolean placeDefences(int row, int column) {
        if (state.isRoundActive()) {
            log("Cannot place defenses while a round is in progress!");
            return false;
        }
        if (selectedDefense == null) return false;

        boolean isLifeTree = LIFE_TREE_NAME.equalsIgnoreCase(selectedDefense.getEntityName());

        if (!isLifeTree && !canAffordDefense(selectedDefense)) {
            log("Not enough defense capacity for this placement");
            return false;
        }
        if (isLifeTree && lifeTreePlaced != null) {
            log("Life Tree already exists");
            return false;
        }
        if (!matrixManager.placeDefense(row, column)) return false;

        Defense placedDef = EntityFactory.cloneDefense(selectedDefense);
        if (placedDef == null) return false;

        placedDef.setCurrentRow(row);
        placedDef.setCurrentColumn(column);

        if (!isLifeTree) {
            EntityFactory.applyDefenseScaling(placedDef, state.getLevel(), rnd);
        }

        Image img = loadAndScale(placedDef.getImagePath());
        PlacedDefense placed = new PlacedDefense(placedDef, row, column, img);
        board.addDefense(placed);

        if (!isLifeTree) {
            waveDefense.add(placedDef);
            state.setDefenseCostUsed(state.getDefenseCostUsed() + Math.max(1, placedDef.getCost()));
        }

        if (isLifeTree) {
            lifeTree = placedDef;
            lifeTreePlaced = placed;
            state.setLifeTreeRow(row);
            state.setLifeTreeColumn(column);
            state.setLifeTreeInitialHealth(placedDef.getHealthPoints());
            state.setBaseHealth(placedDef.getHealthPoints());
            log("Life Tree placed! Health: " + state.getBaseHealth());

            if (sidePanel != null) sidePanel.hideLifeTreeFromCatalog();

            selectedDefense = null;
            board.clearSelectedDefense();
            if (sidePanel != null) sidePanel.deselectDefense();
        }

        if (sidePanel != null) sidePanel.refreshStatusCounters();
        return true;
    }

    public boolean isValidPlacement(int row, int column) {
        return !matrixManager.isOccupied(row, column) && matrixManager.isValidDefensePosition(row, column);
    }

    private boolean canAffordDefense(Defense defense) {
        if (defense == null) return false;
        int cost = Math.max(1, defense.getCost());
        return state.getDefenseCostUsed() + cost <= state.getDefenseCostLimit();
    }

    public boolean isLifeTreePlaced() { return lifeTreePlaced != null; }

    private Image loadAndScale(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw == null) return null;
            int w = (int) (board.getWidth() / 25.0);
            int h = (int) (board.getHeight() / 25.0);
            if (w <= 0 || h <= 0) return raw;
            return raw.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
        } catch (Exception e) {
            return null;
        }
    }

    private void log(String msg) {
        if (sidePanel != null) sidePanel.appendLog(msg);
    }
}
