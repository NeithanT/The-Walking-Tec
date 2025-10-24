package Configuration;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ConfigWindow extends JFrame {
    
    private final GraphicsDevice gd;
    private JFileChooser fileChooser;
    private JFrame parentMenu;
    
    public ConfigWindow() {
        this(null);
    }
    
    public ConfigWindow(JFrame parentMenu) {
        this.parentMenu = parentMenu;
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
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
        }
        this.dispose();
    }
    
    public void chooseFile() throws UnsupportedLookAndFeelException {

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {          
                Image backgroundImage = ImageIO.read(selectedFile);
            } catch (IOException | NullPointerException e) {
                System.err.println("¡Error! No se pudo cargar la imagen de fondo: " + selectedFile);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
           new ConfigWindow();
        });
    } 
}
