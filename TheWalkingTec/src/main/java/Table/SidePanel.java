
package Table;

import GameLogic.GameManager;
import java.awt.Color;
import java.awt.Dimension;
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
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;

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
    private JPanel infoPanel;
    private JPanel imgPanel;
    private TableMain table;
    private GameManager gameManager;
    private JPanel pnlSelected;
    
    public SidePanel(TableMain parentTable) {
    
        table = parentTable;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.add(createDefensesPane());
        scpScrollText.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        this.add(createButtonPanel());
        pnlButtons.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        this.add(createLogArea());
        scpScrollLog.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        btnStart.setBackground(Color.WHITE);
        btnPause.setBackground(Color.WHITE); 
        btnMenu.setBackground(Color.WHITE);
        btnSell.setBackground(Color.WHITE);
        
        selection(btnStart);
        selection(btnPause);
        selection(btnMenu);
        selection(btnSell);

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
        
        try {
            pnlDefenses.add(createDefenseItem("Perro WhatsApp", 15, 5, "/assets/PerroWhatsApp.jpg"));
            pnlDefenses.add(createDefenseItem("Waguri", 20, 7, "/assets/Kaoroku.jpg"));
            pnlDefenses.add(createDefenseItem("Bloque de madera", 25, 0, "/assets/BloqueDeMadera.jpg"));
            pnlDefenses.add(createDefenseItem("Bloque de hierro", 50, 0, "/assets/BloqueDeHierro.jpg"));
            pnlDefenses.add(createDefenseItem("AlienLockin", 25, 10,"/assets/AlienLockIn.jpg"));
            pnlDefenses.add(createDefenseItem("AlienLockin", 25, 10,"/assets/Lebron_James.jpg"));
            pnlDefenses.add(createDefenseItem("Trampa de redstone", 1, 1000,"/assets/TNT.jpg"));
            pnlDefenses.add(createDefenseItem("Goku", 35, 10,"/assets/GokuxD.jpg"));
        } catch(IllegalArgumentException e) {
            System.out.println("No estan presentes algunas iamgenes");
        }
        scpScrollText = new JScrollPane(pnlDefenses);
        scpScrollText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        return scpScrollText;   
    }
    
    private JPanel createDefenseItem(String name, int health, int damage, String imagePath){
        
        JPanel itemPanel = new JPanel();
        itemPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        itemPanel.setPreferredSize(new Dimension(300, 120));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        itemPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
 
        imgPanel = new JPanel();
        imgPanel.setPreferredSize(new Dimension(110, 110));
        imgPanel.setMinimumSize(new Dimension(110, 110));
        imgPanel.setMaximumSize(new Dimension(110, 110));
        imgPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        imgPanel.setLayout(null); 
        
 
        try {
            Image image = ImageIO.read(getClass().getResource(imagePath));
            if (image != null){
                ImageIcon icon = new ImageIcon (image);
             
                Image img = icon.getImage().getScaledInstance(108, 108, Image.SCALE_AREA_AVERAGING);
                JLabel lblImg = new JLabel(new ImageIcon(img));
                lblImg.setBounds(0, 0, 110, 110);
                
                imgPanel.add(lblImg);    
            }
        }catch (IOException e){
            imgPanel.add(new JLabel("No img"));    
        }
        gbc.gridx = 0;
        gbc.weightx = 0; 
        gbc.insets = new Insets(5, 5, 5, 5);
        itemPanel.add(imgPanel, gbc);
        
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3, 1));
        infoPanel.add(new JLabel("Nombre: " + name));
        infoPanel.add(new JLabel("Vida: " + health));
        infoPanel.add(new JLabel("Daño: " + damage));

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        itemPanel.add(infoPanel, gbc);
        
        itemPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event){

                selectDefense(name, itemPanel);
            }
        });

        return itemPanel;   
    }
    
    private void selectDefense(String defenseName, JPanel panel){
        
        if (pnlSelected != null){
            
            pnlSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        }
        
        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        pnlSelected = panel;
        
        if (gameManager != null){
            
            gameManager.setSelectedDefense(defenseName);
        }
        
        System.out.println("Seleccionada: " + defenseName);
        
        
    }
    
    public void deselectDefense(){
        
        if (pnlSelected != null){
            pnlSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            pnlSelected = null;
        }
        
    }
    
    
    
    private void updateSectionSizes(){
        
        int width = this.getWidth();
        int height = this.getHeight();
       
        int defencesHeight = (int)(height * 0.50);
        int buttonsHeight = (int)(height * 0.20);
        int logsHeight = (int)(height * 0.30);
        int defences1Width = (int)(width * 0.30);
        int defences2Width = (int)(width * 0.70);
        
        scpScrollText.setPreferredSize(new Dimension(width, defencesHeight));
        pnlButtons.setPreferredSize(new Dimension(width, buttonsHeight));
        scpScrollLog.setPreferredSize(new Dimension(width, logsHeight));
        imgPanel.setPreferredSize(new Dimension(defences1Width, defencesHeight));
        infoPanel.setPreferredSize(new Dimension(defences2Width, defencesHeight));    
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


        