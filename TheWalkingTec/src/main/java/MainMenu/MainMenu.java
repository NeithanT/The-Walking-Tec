package MainMenu;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MainMenu extends JFrame {
    
    private final GraphicsDevice gd;
    private MenuPanel menu;
    private BackgroundPanel background;
    
    public MainMenu() {
 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);

        background = new BackgroundPanel();
      
        menu = new MenuPanel(this);
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
