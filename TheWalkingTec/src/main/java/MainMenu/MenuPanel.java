package MainMenu;

import Configuration.AdminLoginDialog;
import Configuration.ConfigWindow;
import Configuration.LoadGameDialog;
import Table.TableMain;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MenuPanel extends JPanel {
    
    private JButton btnNewGame;
    private JButton btnLoadGame;
    private JButton btnConfig;
    private JButton btnExit;
    private JPanel  pnlButtons;
    private LoadGameDialog loadDialog;
    private AdminLoginDialog adminDialog;
    private int selectedLevel;
    private JFrame menuFrame;
    
    public MenuPanel(JFrame parentFrame) {
        
        menuFrame = parentFrame;
        this.setLayout(new GridBagLayout());       
        this.setOpaque(false);

        btnNewGame = new JButton("Nueva Partida");        
        btnLoadGame = new JButton("Cargar Partida");
        btnConfig = new JButton("Configuración");
        btnExit = new JButton("Salir");
        
        makeButtonTransparent(btnNewGame);
        makeButtonTransparent(btnLoadGame);
        makeButtonTransparent(btnConfig);
        makeButtonTransparent(btnExit);
        
        btnNewGame.setForeground(Color.WHITE);
        btnLoadGame.setForeground(Color.WHITE);
        btnConfig.setForeground(Color.WHITE);
        btnExit.setForeground(Color.WHITE);
         
        selection(btnNewGame);
        selection(btnLoadGame);
        selection(btnConfig);
        selection(btnExit);
        
        pnlButtons = new JPanel ();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));
        pnlButtons.setOpaque(false);
 
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateButtonSizes();
                updateFontSizes();
                updateButtonPanel();
                updatePosition();
            }
        }); 
        
        btnExit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.exit(0);
            }
        });
        
        btnConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                adminDialog = new AdminLoginDialog(menuFrame);
                boolean authenticated = adminDialog.showAndAuthenticate();
                if (authenticated) {
                    new ConfigWindow(menuFrame);
                    menuFrame.setVisible(false);
                }
            }
        });
        
        btnLoadGame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                loadDialog = new LoadGameDialog(menuFrame);
                selectedLevel = loadDialog.showAndGetSelectedLevel();
                
                if (selectedLevel != -1) {
                    new TableMain(menuFrame, selectedLevel);
                    menuFrame.setVisible(false);
                }
            }
        });
        
        btnNewGame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
        
                new TableMain(menuFrame);
                menuFrame.setVisible(false);
            }
        });
        
    }  
    
    private void updateButtonSizes() {
        
        int width = Math.max(200, (int)(getWidth() * 0.20));
        int height = Math.max(50, (int)(getHeight() * 0.06));
        
        Dimension maxButtonSize = new Dimension(width + 50, height + 10);
        Dimension normalButtonSize = new Dimension(width, height);
  
        btnNewGame.setMaximumSize(maxButtonSize);
        btnNewGame.setPreferredSize(normalButtonSize);
        
        btnLoadGame.setMaximumSize(maxButtonSize);
        btnLoadGame.setPreferredSize(normalButtonSize);
        
        btnConfig.setMaximumSize(maxButtonSize);
        btnConfig.setPreferredSize(normalButtonSize);
        
        btnExit.setMaximumSize(maxButtonSize);
        btnExit.setPreferredSize(normalButtonSize);      
    }
    
    private void updateButtonPanel() {
 
        pnlButtons.removeAll();
        int spacing = Math.max(20, (int)(getHeight() * 0.043));
        
        pnlButtons.add(createBoxedButton(btnNewGame));
        pnlButtons.add(Box.createVerticalStrut(spacing));
        
        pnlButtons.add(createBoxedButton(btnLoadGame));
        pnlButtons.add(Box.createVerticalStrut(spacing));
        
        pnlButtons.add(createBoxedButton(btnConfig));
        pnlButtons.add(Box.createVerticalStrut(spacing));
        
        pnlButtons.add(createBoxedButton(btnExit));
        
        pnlButtons.revalidate();
        pnlButtons.repaint();
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
        this.remove(pnlButtons);
        this.add(pnlButtons, gbc);
        this.revalidate();
        this.repaint();
    }
    
    private void updateFontSizes() {
       
        int fontSize = Math.min(32,Math.max(12, getHeight() / 30));
        Font buttonFont = new Font("Arial", Font.PLAIN, fontSize);
        
        btnNewGame.setFont(buttonFont);
        btnLoadGame.setFont(buttonFont);
        btnConfig.setFont(buttonFont);
        btnExit.setFont(buttonFont);        
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
