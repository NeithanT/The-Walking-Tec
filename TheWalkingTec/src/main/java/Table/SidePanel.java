
package Table;

import Configuration.ConfigManager;
import Defense.Defense;
import GameLogic.GameManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import Defense.DefenseAttacker;
import Defense.DefenseHealer;
import Defense.DefenseType;
import Vanity.RoundedButton;

public class SidePanel extends JPanel {
    
    private RoundedButton btnStart;
    private RoundedButton btnPause;
    private RoundedButton btnMenu;
    private RoundedButton btnSell;
    private JScrollPane scpScrollLog;
    private JScrollPane scpScrollText;
    private JTextArea txaLogs;
    private JPanel pnlButtons;
    private JPanel pnlDefenses;
    private JPanel pnlStatus;
    private JLabel lblDefenseCapacity;
    private JLabel lblZombiesRemaining;

    private TableMain table;
    private GameManager gameManager;
    private JPanel pnlSelected;
    private final ConfigManager configManager;

    private static final String ASSETS_BASE_PATH = "src/main/resources/assets/";
    private static final String COIN_ICON = ASSETS_BASE_PATH + "Coin.png";
    private static final String ZOMBIE_ICON = ASSETS_BASE_PATH + "ZombieHead.png";
    
    public SidePanel(TableMain parentTable) {
    
        table = parentTable;
        
        this.configManager = new ConfigManager();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Modern dark theme background
        this.setBackground(new Color(45, 45, 48));
        
        pnlStatus = createStatusPanel();
        pnlStatus.setBackground(new Color(60, 63, 65));
        pnlStatus.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        this.add(pnlStatus);

        scpScrollText = createDefensesPane();
        scpScrollText.setBackground(new Color(45, 45, 48));
        scpScrollText.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        this.add(scpScrollText);
        
        pnlButtons = createButtonPanel();
        pnlButtons.setBackground(new Color(60, 63, 65));
        pnlButtons.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        this.add(pnlButtons);
        
        
        scpScrollLog = createLogArea();
        scpScrollLog.setBackground(new Color(45, 45, 48));
        scpScrollLog.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

    this.add(scpScrollLog);

        btnStart.addActionListener(evt -> onStartClicked());
        btnPause.addActionListener(evt -> onPauseClicked());

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePanelSize();          
            }
        });
        
        btnMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                // Save current round before going to menu
                if (gameManager != null) {
                    int currentRound = gameManager.getLevel();
                    configManager.saveCurrentRound(currentRound);
                }
                
                if (table != null) {
                    table.goMenu();
                }
                table.dispose();
            }
            
        });
    } 
    
    public void setGameManager(GameManager manager){
        
        this.gameManager = manager;
        refreshStatusCounters();
    }
    
    public void returnToMenu() {
        if (table != null) {
            table.goMenu();
            table.dispose();
        }
    }
    
    private void onStartClicked(){
        if (gameManager == null){
            appendLog("Game manager not ready.");
            return;
        }

        appendLog("Start requested.");
        boolean gameStarted = gameManager.startGame();
        
        // Solo desactivar el botón si el juego realmente inició
        if (gameStarted) {
            btnStart.setEnabled(false);
        } else {
            appendLog("Could not start game. Check requirements.");
        }
    }
    
    public void enableStartButton() {
        if (btnStart != null) {
            btnStart.setEnabled(true);
        }
    }
    
    public void disableStartButton() {
        if (btnStart != null) {
            btnStart.setEnabled(false);
        }
    }
    
    private void onPauseClicked(){
        if (gameManager == null){
            appendLog("Game manager not ready.");
            return;
        }

        gameManager.pauseGame();
        appendLog(gameManager.isGamePaused() ? "Game paused." : "Game resumed.");
    }

    public void appendLog(String message){
        if (txaLogs == null){
            return;
        }
        txaLogs.append(message + "\n");
        txaLogs.setCaretPosition(txaLogs.getDocument().getLength());
    }

    private JPanel createButtonPanel(){
        
        pnlButtons = new JPanel();
        pnlButtons.setLayout(new GridLayout(3,5, 8, 8));
        pnlButtons.setBackground(new Color(60, 63, 65));
        
        // Modern button colors
        Color startColor = new Color(76, 175, 80);      // Green
        Color startHover = new Color(56, 142, 60);
        Color pauseColor = new Color(255, 152, 0);      // Orange
        Color pauseHover = new Color(245, 124, 0);
        Color menuColor = new Color(33, 150, 243);      // Blue
        Color menuHover = new Color(25, 118, 210);
        Color sellColor = new Color(244, 67, 54);       // Red
        Color sellHover = new Color(211, 47, 47);
        
        btnStart = new RoundedButton("Start", startColor, startHover, 15);
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(btnStart.getFont().deriveFont(Font.BOLD, 14f));
        
        btnPause = new RoundedButton("Pause", pauseColor, pauseHover, 15);
        btnPause.setForeground(Color.WHITE);
        btnPause.setFont(btnPause.getFont().deriveFont(Font.BOLD, 14f));
        
        btnMenu = new RoundedButton("Menu", menuColor, menuHover, 15);
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setFont(btnMenu.getFont().deriveFont(Font.BOLD, 14f));
        
        btnSell = new RoundedButton("Sell", sellColor, sellHover, 15);
        btnSell.setForeground(Color.WHITE);
        btnSell.setFont(btnSell.getFont().deriveFont(Font.BOLD, 14f));
        
        pnlButtons.add(new JLabel());
        pnlButtons.add(btnStart);
        pnlButtons.add(new JLabel());
        pnlButtons.add(btnPause);
        pnlButtons.add(new JLabel());
        pnlButtons.add(new JLabel());
        pnlButtons.add(new JLabel());
        pnlButtons.add(new JLabel());
        pnlButtons.add(new JLabel());
        pnlButtons.add(new JLabel());
        pnlButtons.add(new JLabel());
        pnlButtons.add(btnMenu);
        pnlButtons.add(new JLabel());
        pnlButtons.add(btnSell);

        return pnlButtons;
    }
    private JScrollPane createLogArea (){
 
        txaLogs = new JTextArea();
        txaLogs.setEditable(false);
        txaLogs.setBackground(new Color(30, 30, 30));
        txaLogs.setForeground(new Color(204, 204, 204));
        txaLogs.setCaretColor(Color.WHITE);
        txaLogs.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txaLogs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        scpScrollLog = new JScrollPane(txaLogs);
        scpScrollLog.setBackground(new Color(30, 30, 30));
        scpScrollLog.getViewport().setBackground(new Color(30, 30, 30));
        
        return scpScrollLog;
    }
    
    private JScrollPane createDefensesPane() {
        
        pnlDefenses = new JPanel();
        pnlDefenses.setLayout(new BoxLayout(pnlDefenses, BoxLayout.Y_AXIS));
        pnlDefenses.setBackground(new Color(45, 45, 48));
        
        for (Defense d : configManager.getDefenses()){
            
            JPanel item = createDefenseItem(d);
            pnlDefenses.add(item);
        }
        
        JScrollPane scroll = new JScrollPane(pnlDefenses);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBackground(new Color(45, 45, 48));
        scroll.getViewport().setBackground(new Color(45, 45, 48));
        scroll.setBorder(null);
        
        return scroll;   
    }
    
    private JPanel createDefenseItem(Defense def) {
        
        JPanel itemPanel = new JPanel();
        itemPanel.setBackground(new Color(60, 63, 65));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        itemPanel.setPreferredSize(new Dimension(300, 120));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        itemPanel.setLayout(new GridBagLayout());
        
        
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
 
        
        JPanel imgPanel = new JPanel();
        imgPanel.setBackground(new Color(45, 45, 48));
        imgPanel.setPreferredSize(new Dimension(110, 110));
        imgPanel.setMinimumSize(new Dimension(110, 110));
        imgPanel.setMaximumSize(new Dimension(110, 110));
        imgPanel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        imgPanel.setLayout(null); 
        
 
        try {
            if (def.getImagePath() != null && !def.getImagePath().isEmpty()){
                Image image = ImageIO.read(new File(def.getImagePath()));
                if (image != null){
                    
                    Image img = image.getScaledInstance(108, 108, Image.SCALE_AREA_AVERAGING);
                    
             
                    JLabel lblImg = new JLabel(new ImageIcon(img));
                    lblImg.setBounds(0, 0, 110, 110);
                
                    imgPanel.add(lblImg);
                }
                else {
                    imgPanel.add(new JLabel ("No img"));
                }
            }
            else{
                imgPanel.add(new JLabel ("No img"));  
            }     
        }catch (IOException e){
            imgPanel.add(new JLabel("No img"));    
        }
        
        
        gbc.gridx = 0;
        gbc.weightx = 0; 
        gbc.insets = new Insets(5, 5, 5, 5);
        itemPanel.add(imgPanel, gbc);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(60, 63, 65));
        
        String name = def.getEntityName() != null ? def.getEntityName() : "(Sin nombre)";
        JLabel lblName = new JLabel("Name: " + name);
        lblName.setForeground(new Color(187, 187, 187));
        lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 13f));
        lblName.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        infoPanel.add(lblName);
        
        JLabel lblHealth = new JLabel("HP: " + def.getHealthPoints());
        lblHealth.setForeground(new Color(76, 175, 80));
        lblHealth.setFont(lblHealth.getFont().deriveFont(Font.BOLD));
        lblHealth.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        infoPanel.add(lblHealth);
        
        JLabel lblCost = new JLabel("Cost: " + def.getCost());
        lblCost.setForeground(new Color(255, 193, 7));
        lblCost.setFont(lblCost.getFont().deriveFont(Font.BOLD));
        lblCost.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        infoPanel.add(lblCost);
        
        // Show attack stats if it's an attacker
        if (def instanceof DefenseAttacker attack){
            JLabel lblDamage = new JLabel("Attack: " + attack.getAttack());
            lblDamage.setForeground(new Color(244, 67, 54));
            lblDamage.setFont(lblDamage.getFont().deriveFont(Font.BOLD));
            lblDamage.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            infoPanel.add(lblDamage);
            
            JLabel lblRange = new JLabel("Range: " + attack.getRange());
            lblRange.setForeground(new Color(33, 150, 243));
            lblRange.setFont(lblRange.getFont().deriveFont(Font.BOLD));
            lblRange.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            infoPanel.add(lblRange);
        }
        
        // Show heal power if it's a healer
        if (def instanceof DefenseHealer heal){
            JLabel lblHeal = new JLabel("Heal: " + heal.getHealPower());
            lblHeal.setForeground(new Color(156, 39, 176));
            lblHeal.setFont(lblHeal.getFont().deriveFont(Font.BOLD));
            lblHeal.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            infoPanel.add(lblHeal);
        }
        
        // Show defense types
        if (def.getTypes() != null && !def.getTypes().isEmpty()) {
            StringBuilder typesStr = new StringBuilder("Type: ");
            int count = 0;
            for (DefenseType type : def.getTypes()) {
                if (count > 0) typesStr.append(", ");
                typesStr.append(type.toString());
                count++;
            }
            JLabel lblTypes = new JLabel(typesStr.toString());
            lblTypes.setForeground(new Color(150, 150, 150));
            lblTypes.setFont(lblTypes.getFont().deriveFont(Font.PLAIN, 10f));
            lblTypes.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            infoPanel.add(lblTypes);
        }

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        itemPanel.add(infoPanel, gbc);
        
        itemPanel.putClientProperty("defenseDef", def);
        
        // Add hover effect
        itemPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event){
                // No permitir seleccionar defensas si la ronda está activa
                if (gameManager != null && gameManager.isRoundActive()) {
                    appendLog("Cannot select defenses during active round.");
                    return;
                }
                selectDefense(itemPanel);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                // No mostrar hover si la ronda está activa
                if (gameManager != null && gameManager.isRoundActive()) {
                    return;
                }
                
                if (itemPanel != pnlSelected) {
                    itemPanel.setBackground(new Color(75, 80, 85));
                    itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    ));
                    itemPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (itemPanel != pnlSelected) {
                    itemPanel.setBackground(new Color(60, 63, 65));
                    itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    ));
                    itemPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }
            }
        });

        return itemPanel;   
    }
    
    private void selectDefense(JPanel panel){
        
        // Si el panel ya está seleccionado, deseleccionarlo (toggle)
        if (pnlSelected == panel) {
            deselectDefense();
            return;
        }
        
        // Deseleccionar el panel anterior
        if (pnlSelected != null){
            pnlSelected.setBackground(new Color(60, 63, 65));
            pnlSelected.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }
        
        // Seleccionar el nuevo panel
        panel.setBackground(new Color(70, 90, 110));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 3),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        pnlSelected = panel;
        
        if (gameManager != null){
            Defense def = (Defense) panel.getClientProperty("defenseDef");
            gameManager.setSelectedDefense(def);
            appendLog("Seleccionada: " + (def != null ? def.getEntityName() : "null"));
        }   
    }
    
    public void deselectDefense(){
        
        if (pnlSelected != null){
            pnlSelected.setBackground(new Color(60, 63, 65));
            pnlSelected.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            pnlSelected = null;
        }
        
    }
    
    
    
    private JPanel createStatusPanel(){

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2, 10, 0));
        statusPanel.setBackground(new Color(60, 63, 65));

        lblDefenseCapacity = createCounterLabel("0");
        lblZombiesRemaining = createCounterLabel("0");

        statusPanel.add(buildCounterPanel(COIN_ICON, "Towers Left", lblDefenseCapacity));
        statusPanel.add(buildCounterPanel(ZOMBIE_ICON, "Zombies", lblZombiesRemaining));

        return statusPanel;
    }

    private JLabel createCounterLabel(String initialText){
        JLabel label = new JLabel(initialText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setForeground(new Color(187, 187, 187));
        return label;
    }

    private JPanel buildCounterPanel(String iconPath, String title, JLabel valueLabel){

        JPanel container = new JPanel(new BorderLayout(8, 0));
        container.setBackground(new Color(60, 63, 65));
        container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel iconLabel = new JLabel();
        ImageIcon icon = loadIcon(iconPath, 36, 36);
        if (icon != null){
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setText(title);
            iconLabel.setForeground(new Color(187, 187, 187));
        }
        container.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(new Color(60, 63, 65));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        titleLabel.setForeground(new Color(150, 150, 150));
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        container.add(textPanel, BorderLayout.CENTER);

        return container;
    }

    private ImageIcon loadIcon(String path, int width, int height){
        try {
            Image image = ImageIO.read(new File(path));
            if (image != null){
                Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (IOException ex) {
            return null;
        }
        return null;
    }

    public void updateDefenseCapacity(int remaining){
        if (lblDefenseCapacity != null){
            lblDefenseCapacity.setText(String.valueOf(Math.max(0, remaining)));
        }
    }

    public void updateZombiesRemaining(int remaining){
        if (lblZombiesRemaining != null){
            lblZombiesRemaining.setText(String.valueOf(Math.max(0, remaining)));
        }
    }

    public void refreshStatusCounters(){
        if (gameManager != null){
            int remainingCapacity = Math.max(0, gameManager.getDefenseCostLimit() - gameManager.getDefenseCostUsed());
            updateDefenseCapacity(remainingCapacity);
            updateZombiesRemaining(gameManager.getZombiesRemaining());
        }
    }
    
    /**
     * Updates all labels to reflect the current game state
     */
    public void updateAllLabels() {
        if (gameManager != null) {
            int remainingCapacity = Math.max(0, gameManager.getDefenseCostLimit() - gameManager.getDefenseCostUsed());
            updateDefenseCapacity(remainingCapacity);
            updateZombiesRemaining(0); // Reset to 0 when game resets
        } else {
            // Reset to default values when no game manager
            updateDefenseCapacity(0);
            updateZombiesRemaining(0);
        }
    }
    
    public void hideLifeTreeFromCatalog() {
        if (pnlDefenses == null) {
            return;
        }
        
        for (int i = 0; i < pnlDefenses.getComponentCount(); i++) {
            java.awt.Component comp = pnlDefenses.getComponent(i);
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Object property = panel.getClientProperty("defenseDef");
                if (property instanceof Defense) {
                    Defense def = (Defense) property;
                    if (def.getType() == DefenseType.BLOCKS) {
                        panel.setVisible(false);
                        break;
                    }
                }
            }
        }
        
        pnlDefenses.revalidate();
        pnlDefenses.repaint();
    }
    
    public void showLifeTreeInCatalog() {
        if (pnlDefenses == null) {
            return;
        }
        
        for (int i = 0; i < pnlDefenses.getComponentCount(); i++) {
            java.awt.Component comp = pnlDefenses.getComponent(i);
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Object property = panel.getClientProperty("defenseDef");
                if (property instanceof Defense) {
                    Defense def = (Defense) property;
                    if (def.getType() == DefenseType.BLOCKS) {
                        panel.setVisible(true);
                        break;
                    }
                }
            }
        }
        
        pnlDefenses.revalidate();
        pnlDefenses.repaint();
    }

    private void updateSectionSizes(){
        
        int width = this.getWidth();
        int height = this.getHeight();
       
        int statusHeight = (int)(height * 0.15);
        int defencesHeight = (int)(height * 0.45);
        int buttonsHeight = (int)(height * 0.20);
        int logsHeight = (int)(height * 0.20);
        
        if (pnlStatus != null){
            pnlStatus.setPreferredSize(new Dimension(width, statusHeight));
        }
        if (scpScrollText != null){
        scpScrollText.setPreferredSize(new Dimension(width, defencesHeight));
        }
        if (pnlButtons != null){
        pnlButtons.setPreferredSize(new Dimension(width, buttonsHeight));
        }
        if (scpScrollLog != null){
        scpScrollLog.setPreferredSize(new Dimension(width, logsHeight));
        }
    }
    
    private void updatePanelSize() {

        if (getParent() != null) {
            int parentWidth = getParent().getWidth();
            
            int ancho = (int)(parentWidth / 2.7);
            this.setPreferredSize(new Dimension(ancho, 0));

            updateSectionSizes();
            
            revalidate();
        }
    }
}


        