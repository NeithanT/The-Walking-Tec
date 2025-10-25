
package Table;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class TableMain extends JFrame {

    private GraphicsDevice gd = null;
    JFrame parent;
    
    public TableMain() {
        this(null);
    }
    
    public TableMain(JFrame parentMenu) {
        
        parent = parentMenu;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
        this.setUndecorated(true);
        this.setLayout(new BorderLayout());
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);
        
        GameBoard background = new GameBoard();
        SidePanel sidepanel = new SidePanel(this);
        
        this.add(background, BorderLayout.CENTER);
        this.add(sidepanel, BorderLayout.EAST);
        
        
        
        this.setVisible(true);
        
        
    }
    
    public void goMenu() {
        if (parent != null) {
            if (gd.getFullScreenWindow() == this) {
                gd.setFullScreenWindow(null);
            }
            parent.setVisible(true);
            parent.toFront();
            parent.requestFocus();
            gd.setFullScreenWindow(parent);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //new TableMain();
        });
    }
    
}
