package Configuration;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class ConfigWindow extends JFrame {
    
    private final GraphicsDevice gd;
    
    public ConfigWindow() {
        
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);
        
        
       
        BackgroundPanel background = new BackgroundPanel();
      
        ConfigPanel menu = new ConfigPanel();
        background.add(menu, BorderLayout.CENTER);
        
        this.setContentPane(background);
        
        this.setVisible(true);
     
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ConfigWindow();
        });
    } 
}
