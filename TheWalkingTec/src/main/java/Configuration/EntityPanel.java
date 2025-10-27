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
import Defense.DefenseTypeValidator;
import Zombie.Zombie;
import Zombie.ZombieType;
import Zombie.ZombieTypeValidator;
import Vanity.RoundedButton;
import Zombie.ZombieAttacker;
import Zombie.ZombieContact;
import Zombie.ZombieExplosive;
import Zombie.ZombieFlying;
import Zombie.ZombieHealer;
import Zombie.ZombieMediumRange;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
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
    
    private static final int IMAGE_SIZE = 50;
    private static final int BUTTON_SIZE = 22;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final int ENTITY_ROWS_PER_ROW = 3;
    private static final int SPACING = 5;
    
    private ArrayList<EntityRow> entityRows;
    private JPanel headerPanel;
    private JLabel imageLabel;
    private JButton remove;
    private JButton chooser;
    private JButton typeSelectButton;
    private JFileChooser ch;
    private File selectedImageFile;
    private Runnable onContentChanged;
    
    private Zombie currentZombie;
    private Defense currentDefense;
    
    
    public EntityPanel(Zombie zombie) {
        this();
        currentZombie = zombie;
        if (currentZombie.getTypes() == null || currentZombie.getTypes().isEmpty()) {
            currentZombie.setTypes(new java.util.HashSet<>(java.util.Arrays.asList(ZombieType.CONTACT)));
        }
        addZombieSpecificFields();
        populateZombieFields();
        updateLayoutWithRows();
    }
    
    public EntityPanel(Defense defense) {
        this();
        currentDefense = defense;
        if (currentDefense.getTypes() == null || currentDefense.getTypes().isEmpty()) {
            currentDefense.setTypes(new java.util.HashSet<>(java.util.Arrays.asList(DefenseType.BLOCKS)));
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
        EntityRow nameRow = new EntityRow("Nombre: ");
        EntityRow healthRow = new EntityRow("Vida:");
        EntityRow levelRow = new EntityRow("Ronda de Aparicion:");
        EntityRow costRow = new EntityRow("Costo:");
        
        // Aplicar validación numérica a campos que deben ser números
        healthRow.setNumericOnly();
        levelRow.setNumericOnly();
        costRow.setNumericOnly();
        
        entityRows.add(nameRow);
        entityRows.add(healthRow);
        entityRows.add(levelRow);
        entityRows.add(costRow);
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
            String typeLabel = "Tipos: ";
            if (currentZombie != null) {
                typeLabel += formatTypes(currentZombie.getTypes());
            } else if (currentDefense != null) {
                typeLabel += formatTypes(currentDefense.getTypes());
            }
            Color normalColor = new Color(0, 123, 255);
            Color hoverColor = new Color(0, 102, 204);
            typeSelectButton = new RoundedButton(typeLabel, normalColor, hoverColor, 15);
            typeSelectButton.addActionListener(evt -> showTypeSelector());
        }
    }
    
    private String formatTypes(java.util.Set<?> types) {
        if (types == null || types.isEmpty()) {
            return "NONE";
        }
        return types.stream()
                   .map(Object::toString)
                   .collect(java.util.stream.Collectors.joining(", "));
    }
    
    private void showTypeSelector() {
        if (currentZombie != null) {
            showMultipleTypeSelector(ZombieType.class, currentZombie.getTypes(), this::changeZombieTypes);
        } else if (currentDefense != null) {
            showMultipleTypeSelector(DefenseType.class, currentDefense.getTypes(), this::changeDefenseTypes);
        }
    }
    
    private <T extends Enum<?>> void showMultipleTypeSelector(Class<T> enumClass, java.util.Set<T> currentTypes, java.util.function.Consumer<java.util.Set<T>> onConfirm) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        java.util.Map<T, JCheckBox> checkBoxes = new java.util.HashMap<>();
        
        // Create checkbox for each type
        for (T type : enumClass.getEnumConstants()) {
            JCheckBox checkBox = new JCheckBox(type.toString());
            checkBox.setSelected(currentTypes != null && currentTypes.contains(type));
            checkBox.setBackground(Color.WHITE);
            checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
            
            // Add listener to update incompatible checkboxes dynamically
            checkBox.addItemListener(e -> updateCheckBoxStates(checkBoxes, enumClass));
            
            checkBoxes.put(type, checkBox);
            panel.add(checkBox);
        }
        
        // Initial update of checkbox states
        updateCheckBoxStates(checkBoxes, enumClass);
        
        // Add validation label
        JLabel validationLabel = new JLabel(" ");
        validationLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        validationLabel.setForeground(Color.RED);
        panel.add(Box.createVerticalStrut(5));
        panel.add(validationLabel);
        
        // Add confirm button
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            java.util.Set<T> selectedTypes = new java.util.HashSet<>();
            for (java.util.Map.Entry<T, JCheckBox> entry : checkBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedTypes.add(entry.getKey());
                }
            }
            
            // Validate the combination
            String validationError = validateTypeCombination(selectedTypes, enumClass);
            if (validationError != null) {
                validationLabel.setText(validationError);
                return;
            }
            
            onConfirm.accept(selectedTypes);
            SwingUtilities.getWindowAncestor(confirmButton).dispose();
        });
        panel.add(Box.createVerticalStrut(10));
        panel.add(confirmButton);
        
        // Show in dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Seleccionar Tipos", true);
        dialog.add(new JScrollPane(panel));
        dialog.pack();
        dialog.setLocationRelativeTo(typeSelectButton);
        dialog.setVisible(true);
    }
    
    private <T extends Enum<?>> void updateCheckBoxStates(java.util.Map<T, JCheckBox> checkBoxes, Class<T> enumClass) {
        // Get currently selected types
        java.util.Set<T> selectedTypes = new java.util.HashSet<>();
        for (java.util.Map.Entry<T, JCheckBox> entry : checkBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedTypes.add(entry.getKey());
            }
        }
        
        // Get all incompatible types for the selected types
        java.util.Set<T> incompatibleTypes = new java.util.HashSet<>();
        for (T selectedType : selectedTypes) {
            java.util.Set<?> incompatible = getIncompatibleTypesForType(selectedType);
            for (Object type : incompatible) {
                if (enumClass.isInstance(type)) {
                    incompatibleTypes.add((T) type);
                }
            }
        }
        
        // Check if maximum of 2 types are selected
        boolean maxTypesReached = selectedTypes.size() >= 2;
        
        // Enable/disable checkboxes based on incompatibility and max types
        for (java.util.Map.Entry<T, JCheckBox> entry : checkBoxes.entrySet()) {
            JCheckBox checkBox = entry.getValue();
            T type = entry.getKey();
            
            if (checkBox.isSelected()) {
                // Already selected checkboxes remain enabled
                checkBox.setEnabled(true);
                checkBox.setToolTipText(null);
            } else if (maxTypesReached) {
                // Disable if max types (2) already selected
                checkBox.setEnabled(false);
                checkBox.setToolTipText("Máximo 2 tipos permitidos");
            } else if (incompatibleTypes.contains(type)) {
                // Disable if incompatible with selected types
                checkBox.setEnabled(false);
                checkBox.setToolTipText("Incompatible con tipos seleccionados");
            } else {
                // Enable otherwise
                checkBox.setEnabled(true);
                checkBox.setToolTipText(null);
            }
        }
    }
    
    private <T> java.util.Set<?> getIncompatibleTypesForType(T type) {
        if (type instanceof DefenseType) {
            return DefenseTypeValidator.getIncompatibleTypes((DefenseType) type);
        } else if (type instanceof ZombieType) {
            return ZombieTypeValidator.getIncompatibleTypes((ZombieType) type);
        }
        return new java.util.HashSet<>();
    }
    
    private <T> String validateTypeCombination(java.util.Set<T> types, Class<T> enumClass) {
        if (types == null || types.isEmpty()) {
            return "Debe seleccionar al menos un tipo";
        }
        
        if (enumClass == DefenseType.class) {
            java.util.Set<DefenseType> defenseTypes = new java.util.HashSet<>();
            for (T type : types) {
                defenseTypes.add((DefenseType) type);
            }
            DefenseTypeValidator.ValidationResult result = DefenseTypeValidator.validate(defenseTypes);
            return result.isValid() ? null : result.getMessage();
        } else if (enumClass == ZombieType.class) {
            java.util.Set<ZombieType> zombieTypes = new java.util.HashSet<>();
            for (T type : types) {
                zombieTypes.add((ZombieType) type);
            }
            ZombieTypeValidator.ValidationResult result = ZombieTypeValidator.validate(zombieTypes);
            return result.isValid() ? null : result.getMessage();
        }
        
        return null;
    }
    
    private void changeZombieTypes(java.util.Set<ZombieType> newTypes) {
        if (currentZombie == null) {
            return;
        }
        
        currentZombie.setTypes(newTypes);
        typeSelectButton.setText("Tipos: " + formatTypes(newTypes));
        updateTypeSpecificFields();
        updateLayoutWithRows();
    }
    
    private void changeDefenseTypes(java.util.Set<DefenseType> newTypes) {
        if (currentDefense == null) {
            return;
        }
        
        currentDefense.setTypes(newTypes);
        typeSelectButton.setText("Tipos: " + formatTypes(newTypes));
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
            return label.contains("ataque") || label.contains("cura") || label.contains("cantidad") || label.contains("velocidad");
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
        EntityRow speedRow = new EntityRow("Velocidad: ");
        speedRow.setDecimalOnly(); // Velocidad puede tener decimales
        entityRows.add(speedRow);
        
        // Show attack if NOT purely a healer (or if has multiple types)
        // Range is now auto-calculated based on types, no need for manual input
        if (!currentZombie.hasType(ZombieType.HEALER) || currentZombie.getTypes().size() > 1) {
            EntityRow attackRow = new EntityRow("Ataque: ");
            attackRow.setNumericOnly();
            entityRows.add(attackRow);
        }
        
        // Show heal power if has HEALER type
        if (currentZombie.hasType(ZombieType.HEALER)) {
            EntityRow healRow = new EntityRow("Cura: ");
            healRow.setNumericOnly();
            entityRows.add(healRow);
        }
    }
    
    private void addDefenseSpecificFields() {
        // Show heal power if has HEALER type
        if (currentDefense.hasType(DefenseType.HEALER)) {
            EntityRow healRow = new EntityRow("Cura:");
            healRow.setNumericOnly();
            entityRows.add(healRow);
        }
        
        // Show attack if NOT BLOCKS and (NOT HEALER or has multiple types)
        // Range is now auto-calculated based on types, no need for manual input
        if (!currentDefense.hasType(DefenseType.BLOCKS) && 
            (!currentDefense.hasType(DefenseType.HEALER) || currentDefense.getTypes().size() > 1)) {
            EntityRow attackRow = new EntityRow("Ataque:");
            attackRow.setNumericOnly();
            entityRows.add(attackRow);
            
            if (currentDefense.hasType(DefenseType.MULTIPLEATTACK)) {
                EntityRow multipleAttackRow = new EntityRow("Cantidad de ataques:");
                multipleAttackRow.setNumericOnly();
                entityRows.add(multipleAttackRow);
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
        
        headerPanel.add(Box.createVerticalStrut(SPACING));
        headerPanel.add(imageLabel);
        headerPanel.add(Box.createVerticalStrut(SPACING));
        
        if (typeSelectButton != null) {
            headerPanel.add(typeSelectButton);
            headerPanel.add(Box.createVerticalStrut(SPACING));
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
                this.add(Box.createHorizontalStrut(SPACING));
            }
        }
    }
    
    private void updateLayoutWithRows() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height + 180));
        
        headerPanel = createHeaderPanel();
        this.add(headerPanel);
        this.add(Box.createHorizontalStrut(SPACING));
        
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
                notifyContentChanged();
            } else {
                resetImageLabel();
            }
        } catch (IOException ex) {
            System.out.println("While: " + file.getPath());
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
        notifyContentChanged();
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
        
        // Populate movement speed
        if (fieldIndex < entityRows.size()) 
            entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(currentZombie.getMovementSpeed()));
        
        // Populate type-specific fields
        // Attack (if not purely healer or has multiple types)
        // Range is auto-calculated, no field needed
        if (!currentZombie.hasType(ZombieType.HEALER) || currentZombie.getTypes().size() > 1) {
            if (currentZombie instanceof ZombieAttacker attacker) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(attacker.getDamage()));
            } else {
                if (fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
            }
        }
        
        // Heal power (if has HEALER type)
        if (currentZombie.hasType(ZombieType.HEALER)) {
            if (currentZombie instanceof ZombieHealer healer) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(healer.getHealPower()));
            } else if (fieldIndex < entityRows.size()) {
                entityRows.get(fieldIndex++).getTextField().setText("0");
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
        // Heal power (if has HEALER type)
        if (currentDefense.hasType(DefenseType.HEALER)) {
            if (currentDefense instanceof DefenseHealer healer) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(healer.getHealPower()));
            } else if (fieldIndex < entityRows.size()) {
                entityRows.get(fieldIndex++).getTextField().setText("0");
            }
        }
        
        // Attack and MultipleAttack amount (if not BLOCKS and (not HEALER or has multiple types))
        // Range is auto-calculated, no field needed
        if (!currentDefense.hasType(DefenseType.BLOCKS) && 
            (!currentDefense.hasType(DefenseType.HEALER) || currentDefense.getTypes().size() > 1)) {
            if (currentDefense instanceof DefenseAttacker attacker) {
                if (fieldIndex < entityRows.size()) 
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(attacker.getAttack()));
                
                if (currentDefense.hasType(DefenseType.MULTIPLEATTACK) && fieldIndex < entityRows.size() && currentDefense instanceof DefenseMultipleAttack multiAttack) {
                    entityRows.get(fieldIndex++).getTextField().setText(String.valueOf(multiAttack.getAmtOfAttacks()));
                }
            } else {
                if (fieldIndex < entityRows.size()) {
                    entityRows.get(fieldIndex++).getTextField().setText("0");
                }
                if (currentDefense.hasType(DefenseType.MULTIPLEATTACK) && fieldIndex < entityRows.size()) {
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
    
    public boolean allFieldsFilled() {
        for (EntityRow row : entityRows) {
            if (row.getTextField().getText().trim().isEmpty()) {
                return false;
            }
        }
        return hasImageSelected();
    }
    
    public String[] getFieldValues() {
        String[] values = new String[entityRows.size()];
        for (int i = 0; i < entityRows.size(); i++) {
            values[i] = entityRows.get(i).getTextField().getText().trim();
        }
        return values;
    }

    public void setOnContentChanged(Runnable onContentChanged) {
        this.onContentChanged = onContentChanged;
    }

    private void notifyContentChanged() {
        if (onContentChanged != null) {
            onContentChanged.run();
        }
    }

    private boolean hasImageSelected() {
        if (selectedImageFile != null) {
            return true;
        }
        if (currentZombie != null) {
            String imagePath = currentZombie.getImagePath();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                return true;
            }
        }
        if (currentDefense != null) {
            String imagePath = currentDefense.getImagePath();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                return true;
            }
        }
        return false;
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
        java.util.Set<ZombieType> types = currentZombie != null && currentZombie.getTypes() != null ? currentZombie.getTypes() : new java.util.HashSet<>(java.util.Arrays.asList(ZombieType.CONTACT));
        if (types.isEmpty()) {
            types = new java.util.HashSet<>(java.util.Arrays.asList(ZombieType.CONTACT));
        }

        String name = getRowValue("Nombre");
        int health = Integer.parseInt(getRowValue("Vida"));
        int showUp = Integer.parseInt(getRowValue("Ronda de Aparicion"));
        int cost = Integer.parseInt(getRowValue("Costo"));

        Zombie zombie = instantiateZombie(types);
        zombie.setTypes(types);
        zombie.setEntityName(name);
        zombie.setHealthPoints(health);
        zombie.setShowUpLevel(showUp);
        zombie.setCost(cost);
        String movementSpeedValue = getRowValue("Velocidad de Movimiento");
        if (!movementSpeedValue.isEmpty()) {
            zombie.setMovementSpeed(Double.parseDouble(movementSpeedValue));
        }

        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : currentZombie != null ? currentZombie.getImagePath() : null;
        zombie.setImagePath(imagePath);

        if (zombie.hasType(ZombieType.HEALER) && zombie instanceof ZombieHealer healer) {
            String healValue = getRowValue("Cura");
            if (!healValue.isEmpty()) {
                healer.setHealPower(Integer.parseInt(healValue));
            }
        }

        if ((!zombie.hasType(ZombieType.HEALER) || zombie.getTypes().size() > 1) && zombie instanceof ZombieAttacker attacker) {
            String attackValue = getRowValue("Ataque");
            if (!attackValue.isEmpty()) {
                attacker.setDamage(Integer.parseInt(attackValue));
            }
        }

        currentZombie = zombie;
        return zombie;
    }

    public Defense buildDefenseFromFields() throws NumberFormatException {
        java.util.Set<DefenseType> types = currentDefense != null && currentDefense.getTypes() != null ? currentDefense.getTypes() : new java.util.HashSet<>(java.util.Arrays.asList(DefenseType.BLOCKS));
        if (types.isEmpty()) {
            types = new java.util.HashSet<>(java.util.Arrays.asList(DefenseType.BLOCKS));
        }

        String name = getRowValue("Nombre");
        int health = Integer.parseInt(getRowValue("Vida"));
        int showUp = Integer.parseInt(getRowValue("Ronda de Aparicion"));
        int cost = Integer.parseInt(getRowValue("Costo"));

        Defense defense = instantiateDefense(types);
        defense.setTypes(types);
        defense.setEntityName(name);
        defense.setHealthPoints(health);
        defense.setShowUpLevel(showUp);
        defense.setCost(cost);

        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : currentDefense != null ? currentDefense.getImagePath() : null;
        defense.setImagePath(imagePath);

        if (defense.hasType(DefenseType.HEALER) && defense instanceof DefenseHealer healer) {
            String healValue = getRowValue("Cura");
            if (!healValue.isEmpty()) {
                healer.setHealPower(Integer.parseInt(healValue));
            }
        }
        
        if ((!defense.hasType(DefenseType.BLOCKS) && (!defense.hasType(DefenseType.HEALER) || defense.getTypes().size() > 1)) && defense instanceof DefenseAttacker attacker) {
            String attackValue = getRowValue("Ataque");
            if (!attackValue.isEmpty()) {
                attacker.setAttack(Integer.parseInt(attackValue));
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

    private Zombie instantiateZombie(java.util.Set<ZombieType> types) {
        if (types == null || types.isEmpty()) {
            types = new java.util.HashSet<>(java.util.Arrays.asList(ZombieType.CONTACT));
        }
        
        // Determine which constructor to use based on types
        boolean hasHealer = types.contains(ZombieType.HEALER);
        boolean hasAttack = !hasHealer || types.size() > 1;
        
        if (hasHealer && hasAttack) {
            // Hybrid healer - needs both heal and attack capabilities
            ZombieHealer zombie = new ZombieHealer(types, "zombie", 1, 1, 1, 1, 1);
            return zombie;
        } else if (hasHealer) {
            // Pure healer
            return new ZombieHealer(types, "zombie", 1, 1, 1, 1, 1);
        } else {
            // Attacker (can be hybrid like FLYING + EXPLOSIVE)
            return new ZombieAttacker(types, "zombie", 1, 1, 1, 1, 1, 1);
        }
    }

    private Defense instantiateDefense(java.util.Set<DefenseType> types) {
        if (types == null || types.isEmpty()) {
            types = new java.util.HashSet<>(java.util.Arrays.asList(DefenseType.BLOCKS));
        }
        
        // Determine which constructor to use based on types
        boolean hasHealer = types.contains(DefenseType.HEALER);
        boolean hasAttack = !types.contains(DefenseType.BLOCKS) && (!hasHealer || types.size() > 1);
        
        if (hasHealer && hasAttack) {
            // Hybrid healer - needs both heal and attack capabilities
            DefenseHealer defense = new DefenseHealer(types, "defense", 1, 1, 1, 1);
            return defense;
        } else if (hasHealer) {
            // Pure healer
            return new DefenseHealer(types, "defense", 1, 1, 1, 1);
        } else if (hasAttack) {
            // Attacker (can be hybrid like FLYING + EXPLOSIVE or MULTIPLEATTACK)
            if (types.contains(DefenseType.EXPLOSIVE)) {
                return new DefenseExplosive(types, "defense", 1, 1, 1, 1);
            } else if (types.contains(DefenseType.MULTIPLEATTACK)) {
                return new DefenseMultipleAttack(types, "defense", 1, 1, 1, 1, 1, 1);
            } else {
                return new DefenseAttacker(types, "defense", 1, 1, 1, 1, 1);
            }
        } else {
            // Blocks or default
            return new Defense(types, "defense", 1, 1, 1);
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