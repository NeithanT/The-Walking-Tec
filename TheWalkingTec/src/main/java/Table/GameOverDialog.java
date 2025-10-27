package Table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class GameOverDialog extends JDialog {
    
    public enum PlayerChoice {
        RETRY_LEVEL,
        NEXT_LEVEL,
        RETURN_TO_MENU,
        NONE
    }
    
    private PlayerChoice choice = PlayerChoice.NONE;
    private final boolean hasWon;
    
    public GameOverDialog(java.awt.Frame owner, boolean hasWon) {
        super(owner, hasWon ? "¡Victoria!" : "Game Over", true);
        this.hasWon = hasWon;
        initializeComponents();
        pack();
        setMinimumSize(new Dimension(400, 250));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 45, 48));
        
        // Panel superior con el mensaje
        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(new Color(45, 45, 48));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(hasWon ? "¡VICTORIA!" : "GAME OVER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(hasWon ? new Color(50, 205, 50) : new Color(220, 50, 50));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel(hasWon ? 
            "¡Felicidades! Has completado el nivel" : 
            "El Árbol de la Vida ha sido destruido");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        messagePanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        messagePanel.add(messageLabel, gbc);
        
        add(messagePanel, BorderLayout.CENTER);
        
        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(45, 45, 48));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JButton retryButton = createStyledButton("Reintentar Nivel");
        retryButton.addActionListener(e -> {
            choice = PlayerChoice.RETRY_LEVEL;
            dispose();
        });
        buttonPanel.add(retryButton);
        JButton nextButton = createStyledButton("Siguiente Nivel");
        nextButton.addActionListener(e -> {
            choice = PlayerChoice.NEXT_LEVEL;
            dispose();
        });
        buttonPanel.add(nextButton);
        
        
        JButton menuButton = createStyledButton("Menú Principal");
        menuButton.addActionListener(e -> {
            choice = PlayerChoice.RETURN_TO_MENU;
            dispose();
        });
        buttonPanel.add(menuButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 40));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        
        return button;
    }
    
    public PlayerChoice getPlayerChoice() {
        return choice;
    }
    
    public static PlayerChoice showGameOverDialog(java.awt.Frame owner, boolean hasWon) {
        GameOverDialog dialog = new GameOverDialog(owner, hasWon);
        dialog.setVisible(true);
        return dialog.getPlayerChoice();
    }
}
