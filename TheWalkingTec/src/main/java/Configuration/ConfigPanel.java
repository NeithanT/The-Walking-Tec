package Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import Configuration.EntityPanel;
import Defense.Defense;
import Defense.DefenseType;
import Vanity.DefaultFont;
import Vanity.RoundedButton;
import Vanity.RoundedPanel;
import Zombie.Zombie;
import Zombie.ZombieAttacker;
import Zombie.ZombieType;
import java.util.HashSet;
import java.util.Set;

public class ConfigPanel extends JPanel {
    
    private JButton btnZombies;
    private JButton btnDefenses;
    private JButton btnHome;
    private JButton btnAdmins;
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

        initializeComponents();
        initializeData();
        setupHomeButtonListener();
        updateButtonPanel();
        updateFonts();
        addCenteredConfigPanel();
       
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
            addEntityPanel(panel, this.zombies);
        }
        updateCheckmarkButtonState();
    }
    
    private void createRowsDefenses(ArrayList<Defense> defenses) {
        this.defenses.clear();
        for (Defense defense : defenses) {
            EntityPanel panel = new EntityPanel(defense);
            addEntityPanel(panel, this.defenses);
        }
        updateCheckmarkButtonState();
    }

    private void addEntityPanel(EntityPanel panel, ArrayList<EntityPanel> list) {
        list.add(panel);
        attachFieldListener(panel);
        panel.setRemovalListener(this::handleEntityRemoval);
        entityContainer.add(panel);
        entityContainer.add(Box.createVerticalStrut(SPACING));
    }

    private void attachFieldListener(EntityPanel panel) {
        DocumentListener listener = createFieldListener();
        panel.addDocumentListener(listener);
        panel.setOnContentChanged(this::handleFieldChange);
    }

    private DocumentListener createFieldListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleFieldChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleFieldChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleFieldChange();
            }
        };
    }

    private void handleFieldChange() {
        updateCheckmarkButtonState();
    }

    private boolean shouldShowCheckmarkButton() {
        ArrayList<EntityPanel> panels = getEntitiesForCurrentType();
        if (panels.isEmpty()) {
            return true;
        }
        for (EntityPanel panel : panels) {
            if (!panel.allFieldsFilled()) {
                return false;
            }
        }
        return true;
    }

    private void updateCheckmarkButtonState() {
        if (entityContainer == null || btnCheckmark == null) {
            return;
        }
        entityContainer.remove(btnCheckmark);
        if (shouldShowCheckmarkButton()) {
            entityContainer.add(btnCheckmark);
        }
        entityContainer.revalidate();
        entityContainer.repaint();
    }

    private void addNewEntityPanel() {
        if (!shouldShowCheckmarkButton()) {
            return;
        }

        entityContainer.remove(btnCheckmark);

        if (type == SaveType.ZOMBIE) {
            ZombieAttacker zom = new ZombieAttacker("zombie", 1, 1, 1, 1, 1);
            
            EntityPanel panel = new EntityPanel(zom);
            addEntityPanel(panel, zombies);
        } else {
            EntityPanel panel = new EntityPanel(new Defense(DefenseType.BLOCKS, "defense", 1, 1, 1));
            addEntityPanel(panel, defenses);
        }

        updateCheckmarkButtonState();
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
        btnAdmins = createSecondaryButton("Admins");

        btnZombies.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDefenses.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHome.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdmins.setAlignmentX(Component.CENTER_ALIGNMENT);
        
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

        btnAdmins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                openAdminManager();
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
                saveAllEntities();
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

        btnAdmins.setPreferredSize(preferredSize);
        btnAdmins.setMaximumSize(preferredSize);
        
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
        pnlChoices.add(Box.createHorizontalStrut(SPACING));
        pnlChoices.add(createBoxedButton(btnAdmins));
        pnlChoices.add(Box.createHorizontalGlue());
        pnlChoices.add(createBoxedButton(btnHome));
    }
    
    private void buildConfigPanel() {
        pnlConfig.add(Box.createVerticalGlue());
        pnlConfig.add(pnlChoices);
        pnlConfig.add(Box.createVerticalStrut(SPACING));
        pnlConfig.add(listWrapper);
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
    
    private void createCheckmarkButton() {
        btnCheckmark = createAccentButton("+");
        btnCheckmark.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheckmark.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                saveAllEntities();
                addNewEntityPanel();
            }
        });
    }
    
    private void updateFonts() {
        DefaultFont.applyFontToButton(btnZombies);
        DefaultFont.applyFontToButton(btnDefenses);
        DefaultFont.applyFontToButton(btnHome);
        DefaultFont.applyFontToButton(btnAdmins);
        DefaultFont.applyFontToButton(btnCheckmark);
    }
    
    private void saveAllEntities() {
        ArrayList<EntityPanel> currentEntities = getEntitiesForCurrentType();
        if (type == SaveType.ZOMBIE) {
            ArrayList<Zombie> zombiesToSave = new ArrayList<>();
            for (EntityPanel panel : currentEntities) {
                if (!panel.allFieldsFilled()) {
                    continue;
                }
                try {
                    Zombie zombie = panel.buildZombieFromFields();
                    zombiesToSave.add(zombie);
                } catch (NumberFormatException ex) {
                    System.out.println("No se guardo correctamente");
                }
            }
            manager.saveZombies(zombiesToSave);
        } else {
            ArrayList<Defense> defensesToSave = new ArrayList<>();
            for (EntityPanel panel : currentEntities) {
                if (!panel.allFieldsFilled()) {
                    continue;
                }
                try {
                    Defense defense = panel.buildDefenseFromFields();
                    defensesToSave.add(defense);
                } catch (NumberFormatException ex) {
                    System.out.println("No se guardo correctamente");
                }
            }
            manager.saveDefenses(defensesToSave);
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

    private void openAdminManager() {
        AdminManagementDialog dialog = new AdminManagementDialog(configWindow, manager);
        dialog.setVisible(true);
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
    
    private void handleEntityRemoval(EntityPanel panelToRemove) {
        // Remove from UI
        if (type == SaveType.ZOMBIE) {
            zombies.remove(panelToRemove);
        } else {
            defenses.remove(panelToRemove);
        }
        
        // Update UI
        entityContainer.remove(panelToRemove);
        // Remove the spacing box after the panel (if it exists)
        for (int i = 0; i < entityContainer.getComponentCount() - 1; i++) {
            if (entityContainer.getComponent(i) == panelToRemove) {
                if (i + 1 < entityContainer.getComponentCount()) {
                    entityContainer.remove(i + 1);
                }
                break;
            }
        }
        
        entityContainer.revalidate();
        entityContainer.repaint();
        updateCheckmarkButtonState();
        saveAllEntities();
    }
    
}
