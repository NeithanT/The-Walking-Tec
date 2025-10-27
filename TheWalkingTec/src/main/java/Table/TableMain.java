
package Table;

import GameLogic.GameManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class TableMain extends JFrame {

    private GraphicsDevice gd = null;
    JFrame parent;
    private GameManager gameManager;
    
    public TableMain() {
        this(null, 1);
    }
    
    public TableMain(JFrame parentMenu) {
        this(parentMenu, 1);
    }
    
    public TableMain(JFrame parentMenu, int startingLevel) {
        
        parent = parentMenu;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
        this.setUndecorated(true);
        this.setLayout(new BorderLayout());
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);
        
        GameBoard background = new GameBoard();
        SidePanel sidepanel = new SidePanel(this);
        
        gameManager = new GameManager(background, sidepanel);
        gameManager.setParentFrame(this); // Establecer el frame padre para diálogos
        gameManager.setLevel(startingLevel); // Set the starting level
        background.setGameManger(gameManager);
        sidepanel.setGameManager(gameManager);
       // sidepanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        
        
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
            new TableMain();
        });
    }
    
}
