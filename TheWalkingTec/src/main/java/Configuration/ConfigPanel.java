package Configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ConfigPanel extends JPanel {
    
    private JButton btnZombies;
    private JButton btnDefenses;
    private JButton btnHome;
    private JScrollPane scrollArea;
    private JPanel  pnlConfig;
    private JPanel  pnlChoices;
    private JPanel entityContainer;
    private Font font;
    
    ArrayList<EntityPanel> zombies;
    ArrayList<EntityPanel> defenses;
    
    boolean isZombies;
    
    public ConfigPanel() {
        
        //this.setLayout(new GridBagLayout());       
        this.setOpaque(false);

        zombies = new ArrayList<EntityPanel>();
        zombies.add(new EntityPanel());
        
        
        btnZombies = new JButton("Zombies");
        btnDefenses = new JButton("Defenses");
        btnHome = new JButton();
        scrollArea = new JScrollPane();
        
        scrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel entityContainer = new JPanel();
        entityContainer.setLayout(new BoxLayout(entityContainer, BoxLayout.Y_AXIS));
        for (EntityPanel entity : zombies) {
            entityContainer.add(entity);
        }
        scrollArea.setViewportView(entityContainer);
        
        btnZombies.setForeground(Color.BLACK);
        btnDefenses.setForeground(Color.BLACK);
        
        selection(btnZombies);
        selection(btnDefenses);
        selection(btnHome);
        
        pnlChoices = new JPanel();
        pnlChoices.setLayout(new BoxLayout(pnlChoices, BoxLayout.X_AXIS));
        pnlChoices.setOpaque(false);
        
        pnlConfig = new JPanel();
        pnlConfig.setLayout(new BoxLayout(pnlConfig, BoxLayout.Y_AXIS));
        pnlConfig.setOpaque(false);
        
        updateButtonSizes();
        updateFontSizes();
        updateButtonPanel();
        this.add(pnlConfig);
        
        btnHome.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.exit(0);
            }
        });
        
    }
    
    private void updateButtonSizes() {
        
        int width = Math.max(200, (int)(getWidth() * 0.10));
        int height = Math.max(50, (int)(getHeight() * 0.06));
        
        Dimension maxButtonSize = new Dimension(width + 50, height + 10);
        Dimension normalButtonSize = new Dimension(width, height);
  
        btnZombies.setMaximumSize(maxButtonSize);
        btnZombies.setPreferredSize(normalButtonSize);
        
        btnDefenses.setMaximumSize(maxButtonSize);
        btnDefenses.setPreferredSize(normalButtonSize);
    }
    
    private void updateButtonPanel() {
 
        pnlConfig.removeAll();
        pnlChoices.removeAll();
        int spacing = 2;
        
        pnlChoices.add(createBoxedButton(btnZombies));
        pnlChoices.add(Box.createHorizontalStrut(spacing));
        
        pnlChoices.add(createBoxedButton(btnDefenses));
        
        pnlChoices.revalidate();
        pnlChoices.repaint();
        
        pnlConfig.add(pnlChoices);
        pnlConfig.add(Box.createVerticalStrut(spacing));
        
        pnlConfig.add(scrollArea);
        pnlConfig.add(Box.createVerticalStrut(spacing));
        
        pnlConfig.add(btnHome);
        
        pnlConfig.revalidate();
        pnlConfig.repaint();
    }
    
    private void updatePosition() {
        
        int horizontalOffset = (int)(getWidth() * -0.4);
        int verticalOffset = (int)(getHeight() * -0.3);
        
        //gbc.gridx = 10; 
        //gbc.gridy = 10;
        //gbc.gridwidth = 10;
        //gbc.fill = GridBagConstraints.BASELINE;
        //gbc.anchor = GridBagConstraints.NORTH;
        //gbc.insets = new Insets(verticalOffset, horizontalOffset, 0, 0);
        
        //this.remove(pnlConfig);
        
        this.add(pnlConfig);
        
        this.revalidate();
        this.repaint();
    }
    
    private void updateFontSizes() {
        
        int fontSize = Math.min(32,Math.max(12, getHeight() / 30));
        font = new Font("Arial", Font.PLAIN, fontSize);
        
        btnZombies.setFont(font);
        btnDefenses.setFont(font);     
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
                button.setForeground(Color.GREEN);
            }    
        });    
    }
    
}
