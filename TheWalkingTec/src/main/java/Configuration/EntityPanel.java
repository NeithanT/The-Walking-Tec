package Configuration;

import Defense.Defense;
import Defense.DefenseType;
import Zombie.Zombie;
import Zombie.ZombieType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EntityPanel extends JPanel {
    
    private static final int IMAGE_SIZE = 50;
    private static final int BUTTON_SIZE = 30;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Insets INSETS = new Insets(5, 5, 5, 5);
    
    private ArrayList<EntityRow> rows;
    
    
    public EntityPanel(Zombie zombie) {
        this();
        
        if (zombie.getType() != ZombieType.HEALER) {
            rows.add(new EntityRow("Ataque: "));
        } else if (zombie.getType() == ZombieType.EXPLOSIVE) {
            rows.add(new EntityRow("Radio Explosión:"));
        } else {
            rows.add(new EntityRow("Cura: "));
        }
        

        updateLayoutWithRows();
    }
    
    public EntityPanel(Defense defense) {
        this();
        
        if (defense.getType() == DefenseType.HEALER) {
            rows.add(new EntityRow("Cura:"));
        } else if (defense.getType() != DefenseType.BLOCKS) {
            rows.add(new EntityRow("Ataque:"));
            
            if (defense.getType() == DefenseType.MULTIPLEATTACK) {
                rows.add(new EntityRow("Cantidad de ataques:"));
            }
        }
        
        
        updateLayoutWithRows();
    }
    
    public EntityPanel() {
        rows = new ArrayList<>();
        
        createLabels();
        
        for (EntityRow row : rows) {
            //add(row);
        }
        setupLayout();
    }
    
    private void createLabels() {
        rows.add(new EntityRow("Nombre: "));
        rows.add(new EntityRow("Vida:"));
        rows.add(new EntityRow("Aparicion:"));
        rows.add(new EntityRow("Costo:"));
        rows.add(new EntityRow("Rango:"));
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
        this.setLayout(new GridBagLayout());
        this.setBackground(BG_COLOR);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0: image spanning columns 0-4, X at column 5
        JLabel image = createImageLabel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.NONE;
        this.add(image, gbc);
        
        JButton remove = createRemoveButton();
        gbc.gridx = 5;
        gbc.gridwidth = 1;
        this.add(remove, gbc);
        
        // Add all rows dynamically
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        
        int rowIndex = 0;
        int currentRow = 1;
        
        // Add rows in groups of 3 per visual row
        while (rowIndex < rows.size()) {
            gbc.gridy = currentRow;
            
            // Add up to 3 items per row
            for (int col = 0; col < 3 && rowIndex < rows.size(); col++) {
                gbc.gridx = col * 2;
                this.add(rows.get(rowIndex), gbc);
                rowIndex++;
            }
            currentRow++;
        }
    }
    
    private void updateLayoutWithRows() {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0: image spanning columns 0-4, X at column 5
        JLabel image = createImageLabel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.NONE;
        this.add(image, gbc);
        
        JButton remove = createRemoveButton();
        gbc.gridx = 5;
        gbc.gridwidth = 1;
        this.add(remove, gbc);
        
        // Add all rows dynamically
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        
        int rowIndex = 0;
        int currentRow = 1;
        
        // Add rows in groups of 3 per visual row
        while (rowIndex < rows.size()) {
            gbc.gridy = currentRow;
            
            // Add up to 3 items per row
            for (int col = 0; col < 3 && rowIndex < rows.size(); col++) {
                gbc.gridx = col * 2;
                this.add(rows.get(rowIndex), gbc);
                rowIndex++;
            }
            currentRow++;
        }
        
        this.revalidate();
        this.repaint();
    }
    
    public boolean allFieldsFilled() {
        for (EntityRow row : rows) {
            if (row.getTextField().getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public String[] getFieldValues() {
        String[] values = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            values[i] = rows.get(i).getTextField().getText().trim();
        }
        return values;
    }
}