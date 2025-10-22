package Configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ConfigPanel extends JPanel {
    
    private JButton btnNuevaPartida;
    private JButton btnCargarPartida;
    private JButton btnCreditos;
    private JButton btnSalir;
    private JPanel  pnlBotones;
    
    public ConfigPanel(){
        
        this.setLayout(new GridBagLayout());       
        this.setOpaque(false);

        btnNuevaPartida = new JButton("Nueva Partida");        
        btnCargarPartida = new JButton("Cargar Partida");
        btnCreditos = new JButton("Configuración");
        btnSalir = new JButton("Salir");
        
        makeButtonTransparent(btnNuevaPartida);
        makeButtonTransparent(btnCargarPartida);
        makeButtonTransparent(btnCreditos);
        makeButtonTransparent(btnSalir);
        
        btnNuevaPartida.setForeground(Color.WHITE);
        btnCargarPartida.setForeground(Color.WHITE);
        btnCreditos.setForeground(Color.WHITE);
        btnSalir.setForeground(Color.WHITE);
         
        selection(btnNuevaPartida);
        selection(btnCargarPartida);
        selection(btnCreditos);
        selection(btnSalir);
        
        pnlBotones = new JPanel ();
        pnlBotones.setLayout(new BoxLayout(pnlBotones, BoxLayout.Y_AXIS));
        pnlBotones.setOpaque(false);
  
        
        btnSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.exit(0);
            }
        });
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateButtonSizes();
                updateFontSizes();
                updateButtonPanel();
                updatePosition();
            }
        }); 
    }
    
    private void updateButtonSizes() {
        
        int width = Math.max(200, (int)(getWidth() * 0.20));
        int height = Math.max(50, (int)(getHeight() * 0.06));
        
        Dimension maxButtonSize = new Dimension(width + 50, height + 10);
        Dimension normalButtonSize = new Dimension(width, height);
  
        btnNuevaPartida.setMaximumSize(maxButtonSize);
        btnNuevaPartida.setPreferredSize(normalButtonSize);
        
        btnCargarPartida.setMaximumSize(maxButtonSize);
        btnCargarPartida.setPreferredSize(normalButtonSize);
        
        btnCreditos.setMaximumSize(maxButtonSize);
        btnCreditos.setPreferredSize(normalButtonSize);
        
        btnSalir.setMaximumSize(maxButtonSize);
        btnSalir.setPreferredSize(normalButtonSize);      
    }
    
    private void updateButtonPanel(){
 
        pnlBotones.removeAll();
        int spacing = Math.max(20, (int)(getHeight() * 0.043));
        
        pnlBotones.add(createBoxedButton(btnNuevaPartida));
        pnlBotones.add(Box.createVerticalStrut(spacing));
        
        pnlBotones.add(createBoxedButton(btnCargarPartida));
        pnlBotones.add(Box.createVerticalStrut(spacing));
        
        pnlBotones.add(createBoxedButton(btnCreditos));
        pnlBotones.add(Box.createVerticalStrut(spacing));
        
        pnlBotones.add(createBoxedButton(btnSalir));
        
        pnlBotones.revalidate();
        pnlBotones.repaint();
    }
    
    private void updatePosition() {
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        int horizontalOffset = (int)(getWidth() * 0.55);
        int verticalOffset = (int)(getHeight() * 0.2);
        
        gbc.gridx = 5; 
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(verticalOffset, horizontalOffset, 0, 0);
        this.remove(pnlBotones);
        this.add(pnlBotones, gbc);
        this.revalidate();
        this.repaint();
    }
    
    private void updateFontSizes() {
       
        int fontSize = Math.min(32,Math.max(12, getHeight() / 30));
        Font buttonFont = new Font("Arial", Font.PLAIN, fontSize);
        
        btnNuevaPartida.setFont(buttonFont);
        btnCargarPartida.setFont(buttonFont);
        btnCreditos.setFont(buttonFont);
        btnSalir.setFont(buttonFont);        
    }
    
    private Box createBoxedButton (JButton button) {
       
        Box box = Box.createHorizontalBox();  
        box.add(button);
        return box;     
    }
    
    private void makeButtonTransparent(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
    
    private void selection(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.BLACK);   
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }    
        });    
    }
    
}
