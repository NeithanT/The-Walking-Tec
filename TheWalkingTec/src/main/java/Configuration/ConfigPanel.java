package Configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import Configuration.EntityPanel;
import Configuration.FileManager;
import Defense.Defense;
import Zombie.Zombie;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ConfigPanel extends JPanel {
    
    private JButton btnZombies;
    private JButton btnDefenses;
    private JButton btnHome;
    private JButton btnCheckmark;
    
    private JScrollPane scrollArea;
    private JFileChooser fileChooser;
    private JPanel pnlConfig;
    private JPanel pnlChoices;
    private JPanel entityContainer;
    private Font font;
    private ConfigWindow configWindow;
    
    private SaveType type;
    private ArrayList<EntityPanel> zombies;
    private ArrayList<EntityPanel> defenses;
    
    private ConfigManager manager;
    
    private static final int SPACING = 2;
    private static final int MIN_BUTTON_WIDTH = 200;
    private static final int MIN_BUTTON_HEIGHT = 50;
    private static final int BUTTON_WIDTH_OFFSET = 50;
    private static final int BUTTON_HEIGHT_OFFSET = 10;
    private static final int MIN_FONT_SIZE = 12;
    private static final int MAX_FONT_SIZE = 32;
    
    
    public ConfigPanel(ConfigWindow confWindow) {
        
        configWindow = confWindow;
        manager = new ConfigManager();
        type = SaveType.ZOMBIE;
        setOpaque(false);
        
        initializeComponents();
        initializeData();
        setupHomeButtonListener();
        updateButtonPanel();
        createCheckmarkButton();
        add(pnlConfig);
    }
    
    private void initializeData() {
        ArrayList<Zombie> zombies = manager.getZombies();
        ArrayList<Defense> defenses = manager.getDefenses();
        
        if (type == SaveType.ZOMBIE) {
            createRowsZombies(zombies);
        } else {
            createRowsDefenses(defenses);
        }
        
    }
    
    private void createRowsZombies(ArrayList<Zombie> zombies) {
        for (Zombie zombie : zombies) {
            entityContainer.add(new EntityPanel(zombie));
        }
    }
    
    private void createRowsDefenses(ArrayList<Defense> defenses) {
        for (Defense defense : defenses) {
            entityContainer.add(new EntityPanel(defense));
        }
    }
    
    private void reloadList() {
        
    }
    
    private void initializeComponents() {
        initializeButtons();
        initializeScrollArea();
        initializeEntityContainer();
        initializePanels();
        updateButtonSizes();
        updateFontSizes();
    }
    
    private void initializeButtons() {
        btnZombies = new JButton("Zombies");
        btnDefenses = new JButton("Defenses");
        btnHome = new JButton("Home");
        
        setButtonColors(btnZombies, btnDefenses, btnHome);
        addHoverListeners(btnZombies, btnDefenses, btnHome);
    }
    
    private void setButtonColors(JButton... buttons) {
        for (JButton button : buttons) {
            button.setForeground(Color.BLACK);
        }
    }
    
    private void addHoverListeners(JButton... buttons) {
        for (JButton button : buttons) {
            addHoverListener(button);
        }
    }
    
    private void addHoverListener(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(Color.GREEN);   
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setForeground(Color.BLACK);
            }    
        });
    }
    
    private void initializeScrollArea() {
        scrollArea = new JScrollPane();
        scrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    private void initializeEntityContainer() {
        entityContainer = new JPanel();
        entityContainer.setLayout(new BoxLayout(entityContainer, BoxLayout.Y_AXIS));
        scrollArea.setViewportView(entityContainer);
    }
    
    private void initializePanels() {
        pnlChoices = new JPanel();
        pnlChoices.setLayout(new BoxLayout(pnlChoices, BoxLayout.X_AXIS));
        pnlChoices.setOpaque(false);
        
        pnlConfig = new JPanel();
        pnlConfig.setLayout(new BoxLayout(pnlConfig, BoxLayout.Y_AXIS));
        pnlConfig.setOpaque(false);
    }
    
    private void setupHomeButtonListener() {
        btnHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                configWindow.goHome();
                configWindow.dispose();
            }
        });
    }
    
    private ArrayList<EntityPanel> getEntitiesForCurrentType() {
        if (type == SaveType.ZOMBIE) {
            return zombies;
        } else {
            return defenses;
        }
    }
    
    private void updateButtonSizes() {
        int width = calculateButtonWidth();
        int height = calculateButtonHeight();
        
        Dimension maxButtonSize = new Dimension(width + BUTTON_WIDTH_OFFSET, height + BUTTON_HEIGHT_OFFSET);
        Dimension normalButtonSize = new Dimension(width, height);
        
        setButtonDimensions(btnZombies, maxButtonSize, normalButtonSize);
        setButtonDimensions(btnDefenses, maxButtonSize, normalButtonSize);
        setButtonDimensions(btnHome, maxButtonSize, normalButtonSize);
    }
    
    private int calculateButtonWidth() {
        return Math.max(MIN_BUTTON_WIDTH, (int)(getWidth() * 0.10));
    }
    
    private int calculateButtonHeight() {
        return Math.max(MIN_BUTTON_HEIGHT, (int)(getHeight() * 0.06));
    }
    
    private void setButtonDimensions(JButton button, Dimension maxSize, Dimension preferredSize) {
        button.setMaximumSize(maxSize);
        button.setPreferredSize(preferredSize);
    }
    
    private void updateButtonPanel() {
        clearPanels();
        buildChoicesPanel();
        buildConfigPanel();
        refreshPanels();
    }
    
    private void clearPanels() {
        pnlConfig.removeAll();
        pnlChoices.removeAll();
    }
    
    private void buildChoicesPanel() {
        pnlChoices.add(createBoxedButton(btnZombies));
        pnlChoices.add(Box.createHorizontalStrut(SPACING));
        pnlChoices.add(createBoxedButton(btnDefenses));
        pnlChoices.add(Box.createHorizontalGlue());
    }
    
    private void buildConfigPanel() {
        pnlConfig.add(pnlChoices);
        pnlConfig.add(Box.createVerticalStrut(SPACING));
        pnlConfig.add(scrollArea);
        pnlConfig.add(Box.createVerticalStrut(SPACING));
        pnlConfig.add(createHomeButtonBox());
    }
    
    private Box createHomeButtonBox() {
        Box homeBox = Box.createHorizontalBox();
        homeBox.add(btnHome);
        homeBox.add(Box.createHorizontalGlue());
        return homeBox;
    }
    
    private void refreshPanels() {
        pnlChoices.revalidate();
        pnlChoices.repaint();
        pnlConfig.revalidate();
        pnlConfig.repaint();
    }
    
    private void updateFontSizes() {
        int fontSize = calculateFontSize();
        font = new Font("Arial", Font.PLAIN, fontSize);
        
        applyFontToButtons(btnZombies, btnDefenses);
    }
    
    private int calculateFontSize() {
        return Math.min(MAX_FONT_SIZE, Math.max(MIN_FONT_SIZE, getHeight() / 30));
    }
    
    private void applyFontToButtons(JButton... buttons) {
        for (JButton button : buttons) {
            button.setFont(font);
        }
    }
    
    private Box createBoxedButton(JButton button) {
        Box box = Box.createHorizontalBox();  
        box.add(button);
        return box;     
    }
    
    private void saveEntity(EntityPanel panel) {
        try {
            String[] values = panel.getFieldValues();
            Zombie zombie = parseEntityValues(values);
            manager.addZombie(zombie);
        } catch (NumberFormatException ex) {
            System.out.println("No se guardo correctamente");
        }
    }
    
    private Zombie parseEntityValues(String[] values) throws NumberFormatException {
        String name = values[0];
        int health = Integer.parseInt(values[1]);
        int damage = Integer.parseInt(values[2]);
        int showUp = Integer.parseInt(values[3]);
        int cost = Integer.parseInt(values[4]);
        int range = Integer.parseInt(values[5]);
        
        return new Zombie(name, health, damage, showUp, cost, range);
    }
    
    private void createCheckmarkButton() {
        btnCheckmark = new JButton("+");
        btnCheckmark.setBackground(Color.GREEN);
        btnCheckmark.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                saveAllEntities();
            }
        });
    }
    
    private void saveAllEntities() {
        ArrayList<EntityPanel> currentEntities = getEntitiesForCurrentType();
        for (EntityPanel panel : currentEntities) {
            if (!panel.allFieldsFilled()) {
                continue;
            }
            saveEntity(panel);
        }
        isPossibleToAddZombie();
    }
    
    private boolean isPossibleToAddZombie() {
        ArrayList<EntityPanel> panels;
        if (type == SaveType.ZOMBIE) {
            panels = zombies;
        } else {
            panels = defenses;
        }
        for (EntityPanel panel : panels) {
            if (!panel.allFieldsFilled()) {
                return false;
            }
        }
        
        return true;
    }
    
    public void chooseFile() throws UnsupportedLookAndFeelException {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {          
                Image backgroundImage = ImageIO.read(selectedFile);
            } catch (IOException | NullPointerException e) {
                System.err.println("¡Error! No se pudo cargar la imagen de fondo: " + selectedFile);
            }
        }
    }
    
}
