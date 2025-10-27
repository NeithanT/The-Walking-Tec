
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

public class SidePanel extends JPanel {
    
    private JButton btnStart;
    private JButton btnPause;
    private JButton btnMenu;
    private JButton btnSell;
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
        
        pnlStatus = createStatusPanel();
        pnlStatus.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        this.add(pnlStatus);

        scpScrollText = createDefensesPane();
        scpScrollText.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        this.add(scpScrollText);
        
        pnlButtons = createButtonPanel();
        pnlButtons.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        this.add(pnlButtons);
        
        
        scpScrollLog = createLogArea();
        scpScrollLog.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

    this.add(scpScrollLog);

        btnStart.setBackground(Color.WHITE);
        btnPause.setBackground(Color.WHITE); 
        btnMenu.setBackground(Color.WHITE);
        btnSell.setBackground(Color.WHITE);
        
        selection(btnStart);
        selection(btnPause);
        selection(btnMenu);
        selection(btnSell);

        btnStart.addActionListener(evt -> onStartClicked());

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePanelSize();          
            }
        });
        
        btnMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
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
    
    private void onStartClicked(){
        if (gameManager == null){
            appendLog("Game manager not ready.");
            return;
        }

        appendLog("Start requested.");
        gameManager.startGame();
    }

    private void appendLog(String message){
        if (txaLogs == null){
            return;
        }
        txaLogs.append(message + "\n");
        txaLogs.setCaretPosition(txaLogs.getDocument().getLength());
    }

    private JPanel createButtonPanel(){
        
        pnlButtons = new JPanel();
        
        pnlButtons.setLayout(new GridLayout(3,5));
        
        btnStart = new JButton("Start");
        btnPause = new JButton("Pause");
        btnMenu = new JButton("Menu");
        btnSell = new JButton("Sell");
        
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
        
        scpScrollLog = new JScrollPane(txaLogs);
        
        return scpScrollLog;
    }
    
    private JScrollPane createDefensesPane(){
        
        pnlDefenses = new JPanel();
        pnlDefenses.setLayout(new BoxLayout(pnlDefenses, BoxLayout.Y_AXIS));
        
        for (Defense d : configManager.getDefenses()){
            
            JPanel item = createDefenseItem(d);
            pnlDefenses.add(item);
        }
        
        JScrollPane scroll = new JScrollPane(pnlDefenses);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        return scroll;   
    }
    
    private JPanel createDefenseItem(Defense def){
        
        JPanel itemPanel = new JPanel();
        itemPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        itemPanel.setPreferredSize(new Dimension(300, 120));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        itemPanel.setLayout(new GridBagLayout());
        
        
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
 
        
        JPanel imgPanel = new JPanel();
        imgPanel.setPreferredSize(new Dimension(110, 110));
        imgPanel.setMinimumSize(new Dimension(110, 110));
        imgPanel.setMaximumSize(new Dimension(110, 110));
        imgPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
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
        infoPanel.setLayout(new GridLayout(3, 1));
        
        String name = def.getEntityName() != null ? def.getEntityName() : "(Sin nombre)";
        infoPanel.add(new JLabel("Name" + name));
        infoPanel.add(new JLabel("Health: " + def.getHealthPoints()));
        if (def instanceof DefenseAttacker attack){
            infoPanel.add(new JLabel("Damage: " + attack.getAttack()));    
        }
        infoPanel.add(new JLabel("Size: " + def.getCost()));
        if (def instanceof DefenseHealer heal){
            infoPanel.add(new JLabel("Healing Power: " + heal.getHealPower()));    
        }
        if (def instanceof DefenseAttacker range){
            infoPanel.add(new JLabel("Ataque: " + range.getRange()));    
        }

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        itemPanel.add(infoPanel, gbc);
        
        itemPanel.putClientProperty("defenseDef", def);
        
        itemPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event){

                selectDefense(itemPanel);
            }
        });

        return itemPanel;   
    }
    
    private void selectDefense(JPanel panel){
        
        if (pnlSelected != null){
            
            pnlSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        }
        
        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        pnlSelected = panel;
        
        if (gameManager != null){
            Defense def = (Defense) panel.getClientProperty("defenseDef");
            gameManager.setSelectedDefense(def);
            System.out.println("Seleccionada: " + (def != null ? def.getEntityName() : "null"));
        }   
    }
    
    public void deselectDefense(){
        
        if (pnlSelected != null){
            pnlSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            pnlSelected = null;
        }
        
    }
    
    
    
    private JPanel createStatusPanel(){

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2, 10, 0));
        statusPanel.setOpaque(false);

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
        return label;
    }

    private JPanel buildCounterPanel(String iconPath, String title, JLabel valueLabel){

        JPanel container = new JPanel(new BorderLayout(8, 0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel iconLabel = new JLabel();
        ImageIcon icon = loadIcon(iconPath, 36, 36);
        if (icon != null){
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setText(title);
        }
        container.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
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
    
    private void selection(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE); 
                button.setBackground(Color.BLACK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.BLACK);
                button.setBackground(Color.WHITE);
            }   
        });    
    }
}


        