package Configuration;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EntityPanel extends JPanel {
    
    private ArrayList<JLabel> labels;
    
    public EntityPanel() {
        labels = new ArrayList<JLabel>();
        
        labels.add(new JLabel("Hello"));
        
        for (JLabel label : labels) {
            this.add(label);
        }
        this.setBackground(Color.RED);
    }
}
