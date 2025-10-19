package MainMenu;

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
import javax.swing.JLabel;

public class MenuPanel extends JPanel {
    
    private JButton btnNuevaPartida;
    private JButton btnCargarPartida;
    private JButton btnCreditos;
    private JButton btnSalir;
    private JLabel lblTitulo;
    
    public MenuPanel(){
        this.setLayout(new GridBagLayout());    
    
        this.setOpaque(false);
    
        GridBagConstraints gbc = new GridBagConstraints();
       
//        gbc.weightx = 1.0;
//        gbc.weighty = 1.0;
//        gbc.fill = GridBagConstraints.NONE;
//        
//        lblTitulo = new JLabel("BIENVENIDOS AL JUEGO");
//        lblTitulo.setForeground(new Color(35, 0, 255));
//        
//        gbc.gridx = 0; 
//        gbc.gridy = 0;     
//        gbc.gridwidth = GridBagConstraints.REMAINDER;   
//        gbc.anchor = GridBagConstraints.NORTH;  
//        gbc.insets = new Insets(120, 0, 0, 0); 
//        this.add(lblTitulo, gbc);        

        btnNuevaPartida = new JButton("Nueva Partida");        
        btnCargarPartida = new JButton("Cargar Partida");
        btnCreditos = new JButton("Configuración");
        btnSalir = new JButton("Salir");
  
        Dimension maxButtonSize = new Dimension(400, 70);
        Dimension normalButtonSize = new Dimension(300, 65);
  
        btnNuevaPartida.setMaximumSize(maxButtonSize);
        btnNuevaPartida.setPreferredSize(normalButtonSize);
        
        btnCargarPartida.setMaximumSize(maxButtonSize);
        btnCargarPartida.setPreferredSize(normalButtonSize);
        
        btnCreditos.setMaximumSize(maxButtonSize);
        btnCreditos.setPreferredSize(normalButtonSize);
        
        btnSalir.setMaximumSize(maxButtonSize);
        btnSalir.setPreferredSize(normalButtonSize);
        
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
        
        JPanel pnlBotones = new JPanel ();
        pnlBotones.setLayout(new BoxLayout(pnlBotones, BoxLayout.Y_AXIS));
        pnlBotones.setOpaque(false);
  
        pnlBotones.add(createBoxedButton(btnNuevaPartida));
        pnlBotones.add(Box.createVerticalStrut(39));
        
        pnlBotones.add(createBoxedButton(btnCargarPartida));
        pnlBotones.add(Box.createVerticalStrut(37));
        
        pnlBotones.add(createBoxedButton(btnCreditos));
        pnlBotones.add(Box.createVerticalStrut(25));
        
        pnlBotones.add(createBoxedButton(btnSalir));
       

        gbc.gridx = 5; 
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(170, 925, 0, 0);
        this.add(pnlBotones, gbc);
        this.revalidate();
        
          this.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            updateFontSizes();
        }
    });      
    }
    private void updateFontSizes() {
       
        int fontSize = Math.min(32,Math.max(12, getHeight() / 30));
        Font buttonFont = new Font("Arial", Font.PLAIN, fontSize);
        
        btnNuevaPartida.setFont(buttonFont);
        btnCargarPartida.setFont(buttonFont);
        btnCreditos.setFont(buttonFont);
        btnSalir.setFont(buttonFont);
        
//        int titleSize = Math.min(45,Math.max(20, getHeight() / 20));
//        lblTitulo.setFont(new Font("Arial", Font.BOLD, titleSize));
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
