package Configuration;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EntityRow extends JPanel {
    
    JLabel label;
    JTextField textField;
    private final int FIELD_COLUMNS = 15;
    
    public EntityRow() {
    
        
    }
    
    public void createTextField() {
    
        JTextField field = new JTextField(FIELD_COLUMNS);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
}
