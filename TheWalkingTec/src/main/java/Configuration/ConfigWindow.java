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
    JFileChooser fileChooser;
    
    public ConfigWindow() {
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(this);
        
        //BackgroundPanel background = new BackgroundPanel();
      
        ConfigPanel menu = new ConfigPanel();
        //background.add(menu);
        
        this.setContentPane(menu);
        
        this.setVisible(true);
        
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
