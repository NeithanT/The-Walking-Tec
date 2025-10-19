package MainMenu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class MainMenu extends JFrame {
    
    public MainMenu() {
  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.setMinimumSize(new Dimension(800, 600));
        
        this.setExtendedState(MAXIMIZED_BOTH);
       
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
