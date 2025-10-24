
package Table;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class TableMain extends JFrame {

    private GraphicsDevice gd = null;
    
    public TableMain(){
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
        this.setUndecorated(true);
        this.setLayout(new BorderLayout());
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);
        
        GameBoard background = new GameBoard();
        SidePanel sidepanel = new SidePanel();
        
        this.add(background, BorderLayout.CENTER);
        this.add(sidepanel, BorderLayout.EAST);
        
        
        
        this.setVisible(true);
        
        
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TableMain();
    });
    }
    
}
