package Configuration;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EntityRow extends JPanel {
    
    JLabel label;
    JTextField textField;
    private final int FIELD_COLUMNS = 15;
    private static final int IMAGE_SIZE = 50;
    private static final int BUTTON_SIZE = 30;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Insets INSETS = new Insets(5, 5, 5, 5);
    
    public EntityRow(String aspect) {
        
        createLabel(aspect);
        createTextField();
        
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
        JLabel label = new JLabel("Image");
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        label.setPreferredSize(new java.awt.Dimension(IMAGE_SIZE, IMAGE_SIZE));
    }
    
}
