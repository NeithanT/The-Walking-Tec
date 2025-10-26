package Configuration;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class ConfigWindow extends JFrame {
    
    private final GraphicsDevice gd;
    private JFrame parentMenu;
    
    public ConfigWindow() {
        this(null);
    }
    
    public ConfigWindow(JFrame parentMenu) {
        
        this.parentMenu = parentMenu;
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        gd.setFullScreenWindow(this);
        
        ConfigPanel menu = new ConfigPanel(this);
        
        this.setContentPane(menu);
        
        this.setVisible(true);
        
    }
    
    public void goHome() {
        if (parentMenu != null) {
            if (gd.getFullScreenWindow() == this) {
                gd.setFullScreenWindow(null);
            }
            parentMenu.setVisible(true);
            parentMenu.toFront();
            parentMenu.requestFocus();
            gd.setFullScreenWindow(parentMenu);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
           new ConfigWindow();
        });
    } 
}
