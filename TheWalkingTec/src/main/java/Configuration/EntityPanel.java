package Configuration;

import Defense.Defense;
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
        
        switch (zombie.getType()) {
            case ZombieType.CONTACT:
                
                break;
            
            case ZombieType.FLYING:
                
                break;
            case ZombieType.MEDIUMRANGE:
                
                break;
                
            case ZombieType.EXPLOSIVE:
                
                break;
                
            case ZombieType.HEALER:
                
                break;
                
            default:
                break;
        }
    }
    
    public EntityPanel(Defense defense) {
        this();
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
        rows.add(new EntityRow("Daño:"));
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
        
        // Row 1: attributes 0, 1, 2
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 1;
        gbc.gridx = 0;
        this.add(rows.get(0), gbc);
        
        gbc.gridx = 2;
        this.add(rows.get(1), gbc);
        gbc.gridx = 4;
        this.add(rows.get(2), gbc);
        
        // Row 2: attributes 3, 4, 5
        gbc.gridy = 2;
        gbc.gridx = 0;
        this.add(rows.get(3), gbc);
        gbc.gridx = 2;
        this.add(rows.get(4), gbc);
        gbc.gridx = 4;
        this.add(rows.get(5), gbc);
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