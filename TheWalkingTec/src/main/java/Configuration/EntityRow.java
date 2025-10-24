package Configuration;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EntityRow extends JPanel {
    
    JLabel label;
    JTextField textField;
    private final int FIELD_COLUMNS = 15;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    
    public EntityRow(String aspect) {
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(BG_COLOR);
        
        createLabel(aspect);
        createTextField();
        
        add(label);
        add(textField);
        
    }
    
    public void createTextField() {
    
        textField = new JTextField(FIELD_COLUMNS);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    public void createLabel(String aspect) {
        label = new JLabel(aspect);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
}
