package Configuration;

import Defense.Defense;
import Defense.DefenseAttacker;
import Defense.DefenseContact;
import Defense.DefenseExplosive;
import Defense.DefenseFlying;
import Defense.DefenseHealer;
import Defense.DefenseMediumRange;
import Defense.DefenseMultipleAttack;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieType;
import Vanity.RoundedButton;
import Zombie.ZombieAttacker;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieMediumRange;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentListener;

public class EntityPanel extends JPanel {
    
    @FunctionalInterface
    public interface RemovalListener {
        void onEntityRemoved(EntityPanel panel);
    }
    
    private RemovalListener removalListener;
    
    private static final int IMAGE_SIZE = 160;
    private static final int BUTTON_SIZE = 30;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final int ENTITY_ROWS_PER_ROW = 3;
    
    private ArrayList<EntityRow> entityRows;
    private JPanel headerPanel;
    private JLabel imageLabel;
    private JButton remove;
    private JButton chooser;
    private JButton typeSelectButton;
    private JFileChooser ch;
    private File selectedImageFile;
    
    private Zombie currentZombie;
    private Defense currentDefense;
    
    
    public EntityPanel(Zombie zombie) {
        this();
        currentZombie = zombie;
        if (currentZombie.getType() == null) {
            currentZombie.setType(ZombieType.CONTACT);
        }
        addZombieSpecificFields();
        populateZombieFields();
        updateLayoutWithRows();
    }
    
    public EntityPanel(Defense defense) {
        this();
        currentDefense = defense;
        if (currentDefense.getType() == null) {
            currentDefense.setType(DefenseType.BLOCKS);
        }
        addDefenseSpecificFields();
        populateDefenseFields();
        updateLayoutWithRows();
    }
    
    public EntityPanel() {
        entityRows = new ArrayList<>();
        
        createLabels();
        setupLayout();
    }
    
    private void createLabels() {
        entityRows.add(new EntityRow("Nombre: "));
        entityRows.add(new EntityRow("Vida:"));
        entityRows.add(new EntityRow("Ronda de Aparicion:"));
        entityRows.add(new EntityRow("Costo:"));
    }
    
    private void setupLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBackground(BG_COLOR);
        this.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        this.setMaximumSize(new Dimension(4000, getPreferredSize().height + 180));
        
        createHeaderPanel();
        this.add(headerPanel);
        this.add(Box.createHorizontalStrut(10));
        
        addEntityRowsInGroups();
    }
    
    private void createImageLabel() {
        if (imageLabel == null) {
            imageLabel = new JLabel("Image", JLabel.CENTER);
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            imageLabel.setPreferredSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
            imageLabel.setMaximumSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
        }
    }

    private void createImageChooserButton() {
        if (chooser == null) {
            Color normalColor = new Color(40, 167, 69);
            Color hoverColor = new Color(32, 134, 56);
            chooser = new RoundedButton("Elegir Imagen", normalColor, hoverColor, 15);
            chooser.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    openImageChooser();
                }
            });
        }
    }
    
    private void createRemoveButton() {
        Color redColor = new Color(220, 53, 69);
        Color darkRedColor = new Color(200, 35, 51);
        remove = new RoundedButton("X", redColor, darkRedColor, 15);
        remove.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        remove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                handleRemoveClick();
            }
        });
    }
    
    private void handleRemoveClick() {
        if (removalListener != null) {
            removalListener.onEntityRemoved(this);
        }
    }
    
    private void createTypeSelectButton() {
        if (typeSelectButton == null) {
            String typeLabel = "Tipo: ";
            if (currentZombie != null) {
                typeLabel += currentZombie.getType();
            } else if (currentDefense != null) {
                typeLabel += currentDefense.getType();
            }
            Color normalColor = new Color(0, 123, 255);
            Color hoverColor = new Color(0, 102, 204);
            typeSelectButton = new RoundedButton(typeLabel, normalColor, hoverColor, 15);
            typeSelectButton.addActionListener(evt -> showTypeSelector());
        }
    }
    
    private void showTypeSelector() {
        if (currentZombie != null) {
            showGenericTypeSelector(ZombieType.class, currentZombie::getType, this::changeZombieType);
        } else if (currentDefense != null) {
            showGenericTypeSelector(DefenseType.class, currentDefense::getType, this::changeDefenseType);
        }
    }
    
    private <T extends Enum<?>> void showGenericTypeSelector(Class<T> enumClass, java.util.function.Supplier<T> getCurrentType, java.util.function.Consumer<T> onSelect) {
        DefaultListModel<T> model = new DefaultListModel<>();
        for (T type : enumClass.getEnumConstants()) {
            model.addElement(type);
        }
        
        JList<T> typeList = new JList<>(model);
        typeList.setSelectedValue(getCurrentType.get(), true);
        typeList.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                T selectedType = typeList.getSelectedValue();
                if (selectedType != null) {
                    onSelect.accept(selectedType);
                }
            }
        });
        
        JPopupMenu popup = new JPopupMenu();
        popup.add(typeList);
        popup.show(typeSelectButton, 0, typeSelectButton.getHeight());
    }
    
    private void changeZombieType(ZombieType newType) {
        if (currentZombie == null || currentZombie.getType() == newType) {
            return;
        }
        
        currentZombie.setType(newType);
        typeSelectButton.setText("Tipo: " + newType);
        updateTypeSpecificFields();
        updateLayoutWithRows();
    }
    
    private void changeDefenseType(DefenseType newType) {
        if (currentDefense == null || currentDefense.getType() == newType) {
            return;
        }
        
        currentDefense.setType(newType);
        typeSelectButton.setText("Tipo: " + newType);
        updateTypeSpecificFields();
        updateLayoutWithRows();
    }
    
    private void updateTypeSpecificFields() {
        // Store common field values before clearing
        String name = entityRows.size() > 0 ? entityRows.get(0).getTextField().getText() : "";
        String health = entityRows.size() > 1 ? entityRows.get(1).getTextField().getText() : "";
        String showUp = entityRows.size() > 2 ? entityRows.get(2).getTextField().getText() : "";
        String cost = entityRows.size() > 3 ? entityRows.get(3).getTextField().getText() : "";
        
        // Remove all dynamic type-specific fields
        entityRows.removeIf(row -> {
            String label = row.getLabel().getText().toLowerCase();
            return label.contains("ataque") || label.contains("cura") || label.contains("rango") || label.contains("cantidad");
        });
        
        // Add fields based on current entity type
        if (currentZombie != null) {
            addZombieSpecificFields();
        } else if (currentDefense != null) {
            addDefenseSpecificFields();
        }
        
        // Restore common field values
        if (entityRows.size() > 0) entityRows.get(0).getTextField().setText(name);
        if (entityRows.size() > 1) entityRows.get(1).getTextField().setText(health);
        if (entityRows.size() > 2) entityRows.get(2).getTextField().setText(showUp);
        if (entityRows.size() > 3) entityRows.get(3).getTextField().setText(cost);
    }
    
    private void addZombieSpecificFields() {
        if (currentZombie.getType() != ZombieType.HEALER) {
            entityRows.add(new EntityRow("Ataque: "));
            entityRows.add(new EntityRow("Rango: "));
        } else {
            entityRows.add(new EntityRow("Cura: "));
        }
    }
    
    private void addDefenseSpecificFields() {
        if (currentDefense.getType() == DefenseType.HEALER) {
            entityRows.add(new EntityRow("Cura:"));
        } else if (currentDefense.getType() != DefenseType.BLOCKS) {
            entityRows.add(new EntityRow("Ataque:"));
            if (currentDefense.getType() == DefenseType.MULTIPLEATTACK) {
                entityRows.add(new EntityRow("Cantidad de ataques:"));
            }
            if (currentDefense.getType() != DefenseType.MEDIUMRANGE) {
                entityRows.add(new EntityRow("Rango:"));
            }
        }
    }
    
    private JPanel createHeaderPanel() {
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setPreferredSize(new Dimension(150, 250));
        
        createImageLabel();
        createImageChooserButton();
        createRemoveButton();
        
        if (currentZombie != null || currentDefense != null) {
            createTypeSelectButton();
        }
        
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(imageLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        
        if (typeSelectButton != null) {
            headerPanel.add(typeSelectButton);
            headerPanel.add(Box.createVerticalStrut(10));
        }
        
        headerPanel.add(chooser);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(remove);
        headerPanel.add(Box.createVerticalGlue());
        
        return headerPanel;
    }
    
    private void addEntityRowsInGroups() {
        int rowIndex = 0;
        
        while (rowIndex < entityRows.size()) {
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));
            rowPanel.setBackground(BG_COLOR);
            
            for (int i = 0; i < ENTITY_ROWS_PER_ROW && rowIndex < entityRows.size(); i++) {
                rowPanel.add(entityRows.get(rowIndex));
                rowIndex++;
            }
            
            this.add(rowPanel);
            if (rowIndex < entityRows.size()) {
                this.add(Box.createHorizontalStrut(10));
            }
        }
    }
    
    private void updateLayoutWithRows() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height + 180));
        
        headerPanel = createHeaderPanel();
        this.add(headerPanel);
        this.add(Box.createHorizontalStrut(10));
        
        addEntityRowsInGroups();
        
        this.revalidate();
        this.repaint();
    }

    private void openImageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));
        if (selectedImageFile != null && selectedImageFile.getParentFile() != null) {
            chooser.setCurrentDirectory(selectedImageFile.getParentFile());
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            setImageFromFile(file);
        }
    }

    private void setImageFromFile(File file) {
        try {
            Image img = ImageIO.read(file);
            if (img != null) {
                Image scaled = img.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText(null);
                selectedImageFile = file;
                updateEntityImagePath(file.getAbsolutePath());
            } else {
                resetImageLabel();
            }
        } catch (IOException ex) {
            System.err.println("Error loading image: " + ex.getMessage());
            resetImageLabel();
        }
    }

    private void resetImageLabel() {
        if (imageLabel != null) {
            imageLabel.setIcon(null);
            imageLabel.setText("Image");
        }
        selectedImageFile = null;
        updateEntityImagePath(null);
    }

    private void updateEntityImagePath(String path) {
        if (currentZombie != null) {
            currentZombie.setImagePath(path);
        }
        if (currentDefense != null) {
            currentDefense.setImagePath(path);
        }
    }
    
    private void populateZombieFields() {
        if (currentZombie == null) return;
        
        // Populate common fields: Name, Health, Show Up Level, Cost
        int fieldIndex = 0;
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(currentZombie.getEntityName() != null ? currentZombie.getEntityName() : "");
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentZombie.getHealthPoints()));
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentZombie.getShowUpLevel()));
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentZombie.getCost()));
        
        // Populate type-specific fields
        if (currentZombie.getType() == ZombieType.HEALER) {
            if (currentZombie instanceof ZombieHealer healer) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(healer.getHealPower()));
            } else if (fieldIndex < entityRows.size()) {
                entityRows.get(fieldIndex++).getTextField().setText("0");
            }
        } else {
            if (currentZombie instanceof ZombieAttacker attacker) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(attacker.getDamage()));
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(attacker.getRange()));
            } else {
                if (fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
                if (fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
            }
        }
        
        // Load image if path exists
        if (currentZombie.getImagePath() != null && !currentZombie.getImagePath().isEmpty()) {
            setImageFromFile(new java.io.File(currentZombie.getImagePath()));
        }
    }
    
    private void populateDefenseFields() {
        if (currentDefense == null) return;
        
        // Populate common fields: Name, Health, Show Up Level, Cost
        int fieldIndex = 0;
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(currentDefense.getEntityName() != null ? currentDefense.getEntityName() : "");
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentDefense.getHealthPoints()));
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentDefense.getShowUpLevel()));
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentDefense.getCost()));
        
        // Populate type-specific fields
        if (currentDefense.getType() == DefenseType.HEALER) {
            if (currentDefense instanceof DefenseHealer healer) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(healer.getHealPower()));
            } else if (fieldIndex < entityRows.size()) {
                entityRows.get(fieldIndex++).getTextField().setText("0");
            }
        } else if (currentDefense.getType() != DefenseType.BLOCKS) {
            if (currentDefense instanceof DefenseAttacker attacker) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(attacker.getAttack()));
                if (currentDefense.getType() != DefenseType.MEDIUMRANGE && fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(attacker.getRange()));
                
                if (currentDefense.getType() == DefenseType.MULTIPLEATTACK && fieldIndex < entityRows.size() && currentDefense instanceof DefenseMultipleAttack multiAttack) {
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(multiAttack.getAmtOfAttacks()));
                }
            } else {
                if (fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
                if (currentDefense.getType() != DefenseType.MEDIUMRANGE && fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
                if (currentDefense.getType() == DefenseType.MULTIPLEATTACK && fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
            }
        }
        
        if (currentDefense.getImagePath() != null && !currentDefense.getImagePath().isEmpty()) {
            setImageFromFile(new java.io.File(currentDefense.getImagePath()));
        }
    }

    public File getSelectedImageFile() {
        return selectedImageFile;
    }
    
    public ZombieType getZombieType() {
        if (currentZombie != null) {
            return currentZombie.getType();
        }
        return ZombieType.CONTACT;
    }
    
    public DefenseType getDefenseType() {
        if (currentDefense != null) {
            return currentDefense.getType();
        }
        return DefenseType.CONTACT;
    }
    
    public boolean allFieldsFilled() {
        for (EntityRow row : entityRows) {
            if (row.getTextField().getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public String[] getFieldValues() {
        String[] values = new String[entityRows.size()];
        for (int i = 0; i < entityRows.size(); i++) {
            values[i] = entityRows.get(i).getTextField().getText().trim();
        }
        return values;
    }

    private String normalizeLabel(String text) {
        return text == null ? "" : text.replace(":", "").trim().toLowerCase();
    }

    private EntityRow findRowByLabel(String labelKey) {
        String normalizedKey = normalizeLabel(labelKey);
        for (EntityRow row : entityRows) {
            if (normalizeLabel(row.getLabel().getText()).equals(normalizedKey)) {
                return row;
            }
        }
        return null;
    }

    private String getRowValue(String labelKey) {
        EntityRow row = findRowByLabel(labelKey);
        return row != null ? row.getTextField().getText().trim() : "";
    }

    public Zombie buildZombieFromFields() throws NumberFormatException {
        ZombieType type = currentZombie != null && currentZombie.getType() != null ? currentZombie.getType() : getZombieType();
        if (type == null) {
            type = ZombieType.CONTACT;
        }

        String name = getRowValue("Nombre");
        int health = Integer.parseInt(getRowValue("Vida"));
        int showUp = Integer.parseInt(getRowValue("Ronda de Aparicion"));
        int cost = Integer.parseInt(getRowValue("Costo"));

        Zombie zombie = instantiateZombie(type);
        zombie.setType(type);
        zombie.setEntityName(name);
        zombie.setHealthPoints(health);
        zombie.setShowUpLevel(showUp);
        zombie.setCost(cost);

        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : currentZombie != null ? currentZombie.getImagePath() : null;
        zombie.setImagePath(imagePath);

        if (type == ZombieType.HEALER && zombie instanceof ZombieHealer healer) {
            String healValue = getRowValue("Cura");
            if (!healValue.isEmpty()) {
                healer.setHealPower(Integer.parseInt(healValue));
            }
        } else if (zombie instanceof ZombieAttacker attacker) {
            String attackValue = getRowValue("Ataque");
            if (!attackValue.isEmpty()) {
                attacker.setDamage(Integer.parseInt(attackValue));
            }
            String rangeValue = getRowValue("Rango");
            if (!rangeValue.isEmpty()) {
                attacker.setRange(Integer.parseInt(rangeValue));
            }
        }

        currentZombie = zombie;
        return zombie;
    }

    public Defense buildDefenseFromFields() throws NumberFormatException {
        DefenseType type = currentDefense != null && currentDefense.getType() != null ? currentDefense.getType() : getDefenseType();
        if (type == null) {
            type = DefenseType.BLOCKS;
        }

        String name = getRowValue("Nombre");
        int health = Integer.parseInt(getRowValue("Vida"));
        int showUp = Integer.parseInt(getRowValue("Ronda de Aparicion"));
        int cost = Integer.parseInt(getRowValue("Costo"));

        Defense defense = instantiateDefense(type);
        defense.setType(type);
        defense.setEntityName(name);
        defense.setHealthPoints(health);
        defense.setShowUpLevel(showUp);
        defense.setCost(cost);

        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : currentDefense != null ? currentDefense.getImagePath() : null;
        defense.setImagePath(imagePath);

        if (type == DefenseType.HEALER && defense instanceof DefenseHealer healer) {
            String healValue = getRowValue("Cura");
            if (!healValue.isEmpty()) {
                healer.setHealPower(Integer.parseInt(healValue));
            }
        } else if (defense instanceof DefenseAttacker attacker) {
            String attackValue = getRowValue("Ataque");
            if (!attackValue.isEmpty()) {
                attacker.setAttack(Integer.parseInt(attackValue));
            }

            EntityRow rangeRow = findRowByLabel("Rango");
            if (rangeRow != null) {
                String rangeValue = rangeRow.getTextField().getText().trim();
                if (!rangeValue.isEmpty()) {
                    attacker.setRange(Integer.parseInt(rangeValue));
                }
            }

            EntityRow amountRow = findRowByLabel("Cantidad de ataques");
            if (amountRow != null && defense instanceof DefenseMultipleAttack multi) {
                String amountValue = amountRow.getTextField().getText().trim();
                if (!amountValue.isEmpty()) {
                    multi.setAmtOfAttacks(Integer.parseInt(amountValue));
                }
            }
        }

        currentDefense = defense;
        return defense;
    }

    private Zombie instantiateZombie(ZombieType type) {
        if (type == null) {
            return new ZombieAttacker();
        }
        switch (type) {
            case FLYING:
                return new ZombieFlying();
            case MEDIUMRANGE:
                return new ZombieMediumRange();
            case EXPLOSIVE:
                return new ZombieExplosive();
            case HEALER:
                return new ZombieHealer();
            case CONTACT:
            default:
                return new ZombieContact();
        }
    }

    private Defense instantiateDefense(DefenseType type) {
        if (type == null) {
            return new Defense();
        }
        switch (type) {
            case CONTACT:
                return new DefenseContact();
            case MEDIUMRANGE:
                return new DefenseMediumRange();
            case FLYING:
                return new DefenseFlying();
            case EXPLOSIVE:
                return new DefenseExplosive();
            case MULTIPLEATTACK:
                return new DefenseMultipleAttack();
            case HEALER:
                return new DefenseHealer();
            case BLOCKS:
            default:
                return new Defense();
        }
    }

    public void addDocumentListener(DocumentListener listener) {
        for (EntityRow row : entityRows) {
            row.addDocumentListener(listener);
        }
    }
    
    public void setRemovalListener(RemovalListener listener) {
        this.removalListener = listener;
    }
    
    public Zombie getCurrentZombie() {
        return currentZombie;
    }
    
    public Defense getCurrentDefense() {
        return currentDefense;
    }

    public void setCurrentZombie(Zombie zombie) {
        this.currentZombie = zombie;
    }

    public void setCurrentDefense(Defense defense) {
        this.currentDefense = defense;
    }
}