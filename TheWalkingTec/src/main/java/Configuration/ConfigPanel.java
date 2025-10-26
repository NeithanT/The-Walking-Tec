package Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import Configuration.EntityPanel;
import Defense.Defense;
import Vanity.DefaultFont;
import Vanity.RoundedButton;
import Vanity.RoundedPanel;
import Zombie.Zombie;

public class ConfigPanel extends JPanel {
    
    private JButton btnZombies;
    private JButton btnDefenses;
    private JButton btnHome;
    private JButton btnCheckmark;
    
    private JScrollPane scrollArea;
    private JFileChooser fileChooser;
    private RoundedPanel pnlConfig;
    private RoundedPanel pnlChoices;
    private RoundedPanel listWrapper;
    private JPanel entityContainer;
    private ConfigWindow configWindow;
    
    private SaveType type;
    private final ArrayList<EntityPanel> zombies = new ArrayList<>();
    private final ArrayList<EntityPanel> defenses = new ArrayList<>();
    
    private ConfigManager manager;
    
    private static final Color BACKGROUND_TOP = new Color(16, 24, 39);
    private static final Color BACKGROUND_BOTTOM = new Color(30, 41, 59);
    private static final Color PANEL_COLOR = new Color(46, 56, 86, 225);
    private static final Color CHOICES_COLOR = new Color(67, 56, 202, 210);
    private static final Color LIST_COLOR = new Color(30, 64, 175, 200);
    private static final Color PRIMARY_BUTTON_COLOR = new Color(99, 102, 241);
    private static final Color PRIMARY_BUTTON_HOVER = new Color(129, 140, 248);
    private static final Color SECONDARY_BUTTON_COLOR = new Color(14, 165, 233);
    private static final Color SECONDARY_BUTTON_HOVER = new Color(56, 189, 248);
    private static final Color ACCENT_BUTTON_COLOR = new Color(34, 197, 94);
    private static final Color ACCENT_BUTTON_HOVER = new Color(74, 222, 128);
    
    
    private static final int SPACING = 24;
    private static final int CORNER_RADIUS = 28;
    private static final int MIN_BUTTON_WIDTH = 200;
    private static final int MIN_BUTTON_HEIGHT = 50;  
    
    public ConfigPanel(ConfigWindow confWindow) {
        
        configWindow = confWindow;
        manager = new ConfigManager();
        type = SaveType.ZOMBIE;
        
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_TOP);

        initializeComponents();
        initializeData();
        setupHomeButtonListener();
        updateButtonPanel();
        updateFonts();
        addCenteredConfigPanel();
       
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND_TOP, getWidth(), getHeight(), BACKGROUND_BOTTOM);
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
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
        this.zombies.clear();
        for (Zombie zombie : zombies) {
            EntityPanel panel = new EntityPanel(zombie);
            entityContainer.add(panel);
            this.zombies.add(panel);
        }
        entityContainer.add(btnCheckmark);
    }
    
    private void createRowsDefenses(ArrayList<Defense> defenses) {
        this.defenses.clear();
        for (Defense defense : defenses) {
            EntityPanel panel = new EntityPanel(defense);
            entityContainer.add(panel);
            this.defenses.add(panel);
        }
        entityContainer.add(btnCheckmark);
    }
    
    private void reloadList() {
        entityContainer.removeAll();
        zombies.clear();
        defenses.clear();
        
        if (type == SaveType.ZOMBIE) {
            ArrayList<Zombie> zombies = manager.getZombies();
            createRowsZombies(zombies);
        } else {
            ArrayList<Defense> defenses = manager.getDefenses();
            createRowsDefenses(defenses);
        }
        
        entityContainer.revalidate();
        entityContainer.repaint();
    }
    
    private void initializeComponents() {
        initializeButtons();
        createCheckmarkButton();
        initializeEntityContainer();
        initializeScrollArea();
        initializePanels();
        updateButtonSizes();
    }
    
    private void initializeButtons() {
        btnZombies = createPrimaryButton("Zombies");
        btnDefenses = createPrimaryButton("Defenses");
        btnHome = createSecondaryButton("Home");

        btnZombies.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDefenses.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHome.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        setupButtonClickListeners();
    }
    
    private void setupButtonClickListeners() {
        btnZombies.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                switchToZombies();
            }
        });
        
        btnDefenses.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                switchToDefenses();
            }
        });
    }
    
    private void switchToZombies() {
        if (type == SaveType.DEFENSE) {
            type = SaveType.ZOMBIE;
            reloadList();
        }
    }
    
    private void switchToDefenses() {
        if (type == SaveType.ZOMBIE) {
            type = SaveType.DEFENSE;
            reloadList();
        }
    }
    
    
    private void initializeScrollArea() {
        scrollArea = new JScrollPane(entityContainer);
        scrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollArea.setBorder(BorderFactory.createEmptyBorder());
        scrollArea.setOpaque(false);
    }
    
    private void initializeEntityContainer() {
        entityContainer = new JPanel();
        entityContainer.setLayout(new BoxLayout(entityContainer, BoxLayout.Y_AXIS));
        entityContainer.setOpaque(false);
        entityContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
    }
    
    private void initializePanels() {
        pnlChoices = new RoundedPanel(CHOICES_COLOR, CORNER_RADIUS);
        pnlChoices.setLayout(new BoxLayout(pnlChoices, BoxLayout.X_AXIS));
        pnlChoices.setBorder(new EmptyBorder(16, 24, 16, 24));
        pnlChoices.setAlignmentX(Component.CENTER_ALIGNMENT);

        listWrapper = new RoundedPanel(LIST_COLOR, CORNER_RADIUS);
        listWrapper.setLayout(new BorderLayout());
        listWrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        listWrapper.add(scrollArea, BorderLayout.CENTER);
        listWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlConfig = new RoundedPanel(PANEL_COLOR, CORNER_RADIUS);
        pnlConfig.setLayout(new BoxLayout(pnlConfig, BoxLayout.Y_AXIS));
        pnlConfig.setBorder(new EmptyBorder(32, 36, 32, 36));
        pnlConfig.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlConfig.setMaximumSize(new Dimension(960, Integer.MAX_VALUE));
    }

    private void addCenteredConfigPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(40, 80, 40, 80);
        gbc.fill = GridBagConstraints.BOTH;
        add(pnlConfig, gbc);
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
        Dimension preferredSize = new Dimension(MIN_BUTTON_WIDTH, MIN_BUTTON_HEIGHT);
        
        btnZombies.setPreferredSize(preferredSize);
        btnZombies.setMaximumSize(preferredSize);
        
        btnDefenses.setPreferredSize(preferredSize);
        btnDefenses.setMaximumSize(preferredSize);
        
        btnHome.setPreferredSize(preferredSize);
        btnHome.setMaximumSize(preferredSize);
        
        btnCheckmark.setPreferredSize(new Dimension(1600, 60));
        btnCheckmark.setMaximumSize(new Dimension(1600, 60));
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
        pnlConfig.add(Box.createVerticalGlue());
        pnlConfig.add(pnlChoices);
        pnlConfig.add(Box.createVerticalStrut(SPACING));
        pnlConfig.add(listWrapper);
        pnlConfig.add(Box.createVerticalStrut(SPACING));
        pnlConfig.add(createHomeButtonBox());
        pnlConfig.add(Box.createVerticalGlue());

    }
    
    private Box createHomeButtonBox() {
        Box homeBox = Box.createHorizontalBox();
        homeBox.setOpaque(false);
        homeBox.add(btnHome);
        homeBox.add(Box.createHorizontalGlue());
        homeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        return homeBox;
    }
    
    private void refreshPanels() {
        listWrapper.revalidate();
        listWrapper.repaint();
        pnlChoices.revalidate();
        pnlChoices.repaint();
        pnlConfig.revalidate();
        pnlConfig.repaint();
    }
    
    private Box createBoxedButton(JButton button) {
        Box box = Box.createHorizontalBox();
        box.setOpaque(false);
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (button != null) {
            box.add(button);
        }
        return box;
    }

    private JButton createPrimaryButton(String text) {
        return new RoundedButton(text, PRIMARY_BUTTON_COLOR, PRIMARY_BUTTON_HOVER, CORNER_RADIUS);
    }

    private JButton createSecondaryButton(String text) {
        return new RoundedButton(text, SECONDARY_BUTTON_COLOR, SECONDARY_BUTTON_HOVER, CORNER_RADIUS);
    }

    private JButton createAccentButton(String text) {
        return new RoundedButton(text, ACCENT_BUTTON_COLOR, ACCENT_BUTTON_HOVER, CORNER_RADIUS);
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
        btnCheckmark = createAccentButton("+");
        btnCheckmark.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheckmark.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                saveAllEntities();
            }
        });
    }
    
    private void updateFonts() {
        DefaultFont.applyFontToButton(btnZombies);
        DefaultFont.applyFontToButton(btnDefenses);
        DefaultFont.applyFontToButton(btnHome);
        DefaultFont.applyFontToButton(btnCheckmark);
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
