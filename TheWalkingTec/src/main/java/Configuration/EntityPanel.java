package Configuration;

import Defense.Defense;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieType;
import Vanity.RoundedButton;
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
    private File selectedImageFile;
    private Zombie currentZombie;
    private Defense currentDefense;
    
    
    public EntityPanel(Zombie zombie) {
        this();
        currentZombie = zombie;
        addZombieSpecificFields();
        updateLayoutWithRows();
    }
    
    public EntityPanel(Defense defense) {
        this();
        currentDefense = defense;
        addDefenseSpecificFields();
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
}