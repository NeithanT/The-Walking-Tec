package Configuration;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

public class EntityRow extends JPanel {
    
    JLabel label;
    JTextField textField;
    private final int FIELD_COLUMNS = 15;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    
    public EntityRow(String aspect) {
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(BG_COLOR);
        
        label = new JLabel(aspect);
        createTextField();
        
        add(label);
        add(textField);
        
    }
    
    public EntityRow(String aspect, String field) {
        this(aspect);
        
    }
    
    public void createTextField() {
    
        textField = new JTextField(FIELD_COLUMNS);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    public JTextField getTextField() {
        return textField;
    }

    public void addDocumentListener(DocumentListener listener) {
        textField.getDocument().addDocumentListener(listener);
    }
}