package Configuration;

import Defense.Defense;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EntityPanel extends JPanel {
    
    private static final int IMAGE_SIZE = 50;
    private static final int BUTTON_SIZE = 30;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Insets INSETS = new Insets(5, 5, 5, 5);
    private static final int ENTITY_ROWS_PER_ROW = 3;
    
    private ArrayList<EntityRow> entityRows;
    
    
    public EntityPanel(Zombie zombie) {
        this();
        
        if (zombie.getType() != ZombieType.HEALER) {
            entityRows.add(new EntityRow("Ataque: "));
        } else if (zombie.getType() == ZombieType.EXPLOSIVE) {
            entityRows.add(new EntityRow("Radio de explosion:"));
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
        entityRows.add(new EntityRow("Aparicion:"));
        entityRows.add(new EntityRow("Costo:"));
    }
    
    private JLabel createImageLabel() {
        JLabel label = new JLabel("Image");
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        label.setPreferredSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
        return label;
    }
    
    private JButton createRemoveButton() {
        JButton btn = new JButton("X");
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        return btn;
    }
    
    private void setupLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(BG_COLOR);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel with image and remove button
        JPanel headerPanel = createHeaderPanel();
        this.add(headerPanel);
        this.add(Box.createVerticalStrut(10));
        
        // Add EntityRows in groups of 3 per row
        addEntityRowsInGroups();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setBackground(BG_COLOR);
        
        JLabel image = createImageLabel();
        JButton remove = createRemoveButton();
        
        headerPanel.add(image);
        headerPanel.add(remove);
        
        return headerPanel;
    }
    
    private void addEntityRowsInGroups() {
        int rowIndex = 0;
        
        while (rowIndex < entityRows.size()) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            rowPanel.setBackground(BG_COLOR);
            
            // Add up to 3 EntityRows per horizontal row
            for (int i = 0; i < ENTITY_ROWS_PER_ROW && rowIndex < entityRows.size(); i++) {
                rowPanel.add(entityRows.get(rowIndex));
                rowIndex++;
            }
            
            this.add(rowPanel);
        }
    }
    
    private void updateLayoutWithRows() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Header panel with image and remove button
        JPanel headerPanel = createHeaderPanel();
        this.add(headerPanel);
        this.add(Box.createVerticalStrut(10));
        
        // Add EntityRows in groups of 3 per row
        addEntityRowsInGroups();
        
        this.revalidate();
        this.repaint();
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
}