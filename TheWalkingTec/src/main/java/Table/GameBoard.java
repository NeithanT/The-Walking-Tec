package Table;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class GameBoard extends JPanel {
    
    private Image backgroundImage;
    
    private static final String IMAGE_PATH = "/assets/tablero.png";
    
    public GameBoard(){
        
        this.setLayout(null);
        
        try {          
            backgroundImage = ImageIO.read(getClass().getResource(IMAGE_PATH));
        } 
        catch (IOException | NullPointerException e) {
            System.err.println("¡Error! No se pudo cargar la imagen de fondo: " + IMAGE_PATH);
        
        }
    
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } 
        else 
        {     
            g.setColor(java.awt.Color.BLACK); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
}