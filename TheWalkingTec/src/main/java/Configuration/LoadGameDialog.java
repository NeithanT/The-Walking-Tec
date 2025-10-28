package Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class LoadGameDialog extends JDialog {
    
    private ConfigManager configManager;
    private int selectedLevel;
    private JPanel gamesPanel;
    
    public LoadGameDialog(JFrame parent) {
        super(parent, "Load Game", true);
        this.configManager = new ConfigManager();
        this.selectedLevel = -1;
        
        initComponents();
        loadGames();
        
        setSize(600, 400);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 45, 48));
        
        // Title
        JLabel titleLabel = new JLabel("Select a Saved Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);
        
        // Games panel with scroll
        gamesPanel = new JPanel();
        gamesPanel.setLayout(new GridLayout(0, 1, 10, 10));
        gamesPanel.setBackground(new Color(45, 45, 48));
        gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JScrollPane scrollPane = new JScrollPane(gamesPanel);
        scrollPane.setBackground(new Color(45, 45, 48));
        scrollPane.getViewport().setBackground(new Color(45, 45, 48));
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(45, 45, 48));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        
        JButton cancelButton = createStyledButton("Cancel", new Color(244, 67, 54));
        cancelButton.addActionListener(e -> {
            selectedLevel = -1;
            dispose();
        });
        
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadGames() {
        ArrayList<Integer> gameLevels = configManager.getGameLevels();
        
        if (gameLevels == null || gameLevels.isEmpty()) {
            JLabel noGamesLabel = new JLabel("No saved games found", SwingConstants.CENTER);
            noGamesLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            noGamesLabel.setForeground(new Color(150, 150, 150));
            gamesPanel.add(noGamesLabel);
            return;
        }
        
        // Display each saved game level
        for (int i = 0; i < gameLevels.size(); i++) {
            int level = gameLevels.get(i);
            JPanel gameCard = createGameCard(level, i);
            gamesPanel.add(gameCard);
        }
    }
    
    private JPanel createGameCard(int level, int index) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(new Color(60, 63, 65));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setPreferredSize(new Dimension(500, 80));
        card.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Game info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Saved Game " + (index + 1));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel levelLabel = new JLabel("Level: " + level);
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        levelLabel.setForeground(new Color(187, 187, 187));
        
        infoPanel.add(titleLabel);
        infoPanel.add(levelLabel);
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Load button
        JButton loadButton = createStyledButton("Load", new Color(76, 175, 80));
        loadButton.setPreferredSize(new Dimension(100, 50));
        loadButton.addActionListener(e -> {
            selectedLevel = level;
            dispose();
        });
        
        card.add(loadButton, BorderLayout.EAST);
        
        // Hover effects
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(75, 80, 85));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(60, 63, 65));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(80, 80, 80), 2),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
        });
        
        return card;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Shows the dialog and returns the selected level
     * @return the selected level, or -1 if cancelled
     */
    public int showAndGetSelectedLevel() {
        setVisible(true);
        return selectedLevel;
    }
}
