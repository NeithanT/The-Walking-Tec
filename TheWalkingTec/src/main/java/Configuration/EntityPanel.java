package Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
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
    private static final int FIELD_COLUMNS = 15;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Insets INSETS = new Insets(5, 5, 5, 5);
    
    private ArrayList<JLabel> labels;
    private ArrayList<JTextField> textFields;
    
    public EntityPanel() {
        labels = new ArrayList<>();
        textFields = new ArrayList<>();
        
        createLabels();
        createTextFields();
        setupLayout();
    }
    
    private void createLabels() {
        labels.add(new JLabel("Nombre:"));
        labels.add(new JLabel("Vida:"));
        labels.add(new JLabel("Daño:"));
        labels.add(new JLabel("Aparicion:"));
        labels.add(new JLabel("Costo:"));
        labels.add(new JLabel("Rango:"));
        labels.add(new JLabel("Tipo:"));
    }
    
    private void createTextFields() {
        for (int i = 0; i < labels.size(); i++) {
            JTextField field = new JTextField(FIELD_COLUMNS);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            textFields.add(field);
        }
    }
    
    private JLabel createImageLabel() {
        JLabel label = new JLabel("Image");
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        label.setPreferredSize(new java.awt.Dimension(IMAGE_SIZE, IMAGE_SIZE));
        return label;
    }
    
    private JButton createRemoveButton() {
        JButton btn = new JButton("X");
        btn.setPreferredSize(new java.awt.Dimension(BUTTON_SIZE, BUTTON_SIZE));
        return btn;
    }
    
    private JPanel createRowPanel(int[] indices) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(BG_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        
        for (int i : indices) {
            rowPanel.add(labels.get(i), gbc);
            gbc.gridx++;
            rowPanel.add(textFields.get(i), gbc);
            gbc.gridx++;
        }
        
        return rowPanel;
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
        this.add(labels.get(0), gbc);
        gbc.gridx = 1;
        this.add(textFields.get(0), gbc);
        gbc.gridx = 2;
        this.add(labels.get(1), gbc);
        gbc.gridx = 3;
        this.add(textFields.get(1), gbc);
        gbc.gridx = 4;
        this.add(labels.get(2), gbc);
        gbc.gridx = 5;
        this.add(textFields.get(2), gbc);
        
        // Row 2: attributes 3, 4, 5
        gbc.gridy = 2;
        gbc.gridx = 0;
        this.add(labels.get(3), gbc);
        gbc.gridx = 1;
        this.add(textFields.get(3), gbc);
        gbc.gridx = 2;
        this.add(labels.get(4), gbc);
        gbc.gridx = 3;
        this.add(textFields.get(4), gbc);
        gbc.gridx = 4;
        this.add(labels.get(5), gbc);
        gbc.gridx = 5;
        this.add(textFields.get(5), gbc);
        
        // Row 3: attribute 6
        gbc.gridy = 3;
        gbc.gridx = 0;
        this.add(labels.get(6), gbc);
        gbc.gridx = 1;
        this.add(textFields.get(6), gbc);
    }
}
