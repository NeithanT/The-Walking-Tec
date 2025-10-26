package Configuration;

import Defense.Defense;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
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
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentListener;

public class EntityPanel extends JPanel {
    
    private static final int IMAGE_SIZE = 50;
    private static final int BUTTON_SIZE = 30;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final int ENTITY_ROWS_PER_ROW = 3;
    
    private ArrayList<EntityRow> entityRows;
    private JLabel imageLabel;
    private JButton chooseImageButton;
    private File selectedImageFile;
    
    
    public EntityPanel(Zombie zombie) {
        this();
        
        if (zombie.getType() != ZombieType.HEALER) {
            entityRows.add(new EntityRow("Ataque: "));
            entityRows.add(new EntityRow("Rango: "));
        } else {
            entityRows.add(new EntityRow("Cura: "));
        }
        
        updateLayoutWithRows();
    }
    
    public EntityPanel(Defense defense) {
        this();
        
        if (defense.getType() == DefenseType.HEALER) {
            entityRows.add(new EntityRow("Cura:"));
        } else if (defense.getType() != DefenseType.BLOCKS) {
            entityRows.add(new EntityRow("Ataque:"));
            if (defense.getType() == DefenseType.MULTIPLEATTACK) {
                entityRows.add(new EntityRow("Cantidad de ataques:"));
            }
            if (defense.getType() != DefenseType.MEDIUMRANGE) {
                entityRows.add(new EntityRow("Rango:"));
            }
        }
        
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
    
    private JLabel createImageLabel() {
        if (imageLabel == null) {
            imageLabel = new JLabel("Image", JLabel.CENTER);
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            imageLabel.setPreferredSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
        }
        return imageLabel;
    }

    private JButton createImageChooserButton() {
        if (chooseImageButton == null) {
            chooseImageButton = new JButton("Elegir Imagen");
            chooseImageButton.addActionListener(evt -> openImageChooser());
            chooseImageButton.setMargin(new Insets(5, 10, 5, 10));
        }
        return chooseImageButton;
    }
    
    private JButton createRemoveButton() {
        JButton btn = new JButton("X");
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        return btn;
    }
    
    private void setupLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBackground(BG_COLOR);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = createHeaderPanel();
        this.add(headerPanel);
        this.add(Box.createHorizontalStrut(10));
        
        // Add EntityRows in groups of 3 per row
        addEntityRowsInGroups();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setBackground(BG_COLOR);
        
        JLabel image = createImageLabel();
        JButton chooser = createImageChooserButton();
        JButton remove = createRemoveButton();
        
        headerPanel.add(image);
        headerPanel.add(chooser);
        headerPanel.add(remove);
        
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
        
        JPanel headerPanel = createHeaderPanel();
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
}