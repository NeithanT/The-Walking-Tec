package MainMenu;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MainMenu extends JFrame {
    
    private final GraphicsDevice gd;
    
    public MainMenu() {
 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);

        BackgroundPanel background = new BackgroundPanel();
      
        MenuPanel menu = new MenuPanel();
        background.add(menu, BorderLayout.CENTER);
        
        this.setContentPane(background);
        
        this.setVisible(true);
     
    }
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new MainMenu();
    });
} 
}
