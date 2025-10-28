package Table;

import GameLogic.CombatLog;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * BattleSummaryDialog - Shows complete battle statistics at the end of a round
 */
public class BattleSummaryDialog extends JDialog {
    
    private static boolean isDialogOpen = false;
    
    public BattleSummaryDialog(JFrame parent, CombatLog log, boolean victory) {
        super(parent, victory ? "¡VICTORIA!" : "DERROTA", true);
        
        setLayout(new BorderLayout(10, 10));
        setSize(900, 700);
        setLocationRelativeTo(parent);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(30, 30, 30));
        
        // Title panel
        JPanel titlePanel = createTitlePanel(log, victory);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Content panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        tabbedPane.setBackground(new Color(45, 45, 45));
        tabbedPane.setForeground(Color.WHITE);
        
        // Tab 1: Defenses
        JPanel defensesPanel = createEntitiesPanel(log, true);
        tabbedPane.addTab("Defensas", defensesPanel);
        
        // Tab 2: Zombies
        JPanel zombiesPanel = createEntitiesPanel(log, false);
        tabbedPane.addTab("Zombies", zombiesPanel);
        
        // Tab 3: Combat Log
        JPanel logPanel = createCombatLogPanel(log);
        tabbedPane.addTab("Registro de Combate", logPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JButton closeButton = new JButton("Continuar");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(victory ? new Color(76, 175, 80) : new Color(244, 67, 54));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 30));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        // Don't call setVisible here - it will be called by showSummary()
    }
    
    private JPanel createTitlePanel(CombatLog log, boolean victory) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(victory ? new Color(27, 94, 32) : new Color(183, 28, 28));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(victory ? "¡VICTORIA!" : "DERROTA");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel levelLabel = new JLabel("Nivel " + log.getLevel() + " - Duración: " + formatDuration(log.getBattleDuration()));
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(levelLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEntitiesPanel(CombatLog log, boolean showDefenses) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Filter entities
        ArrayList<CombatLog.EntityCombatStats> entities = new ArrayList<>();
        for (CombatLog.EntityCombatStats stats : log.getAllStats()) {
            if (stats.isDefense == showDefenses) {
                entities.add(stats);
            }
        }
        
        // Create table
        String[] columnNames = {
            "Nombre", "Vida Inicial", "Vida Final", "Estado", 
            "Daño Causado", "Ataques", "Kills", "Daño Recibido",
            "Posición"
        };
        
        Object[][] data = new Object[entities.size()][columnNames.length];
        for (int i = 0; i < entities.size(); i++) {
            CombatLog.EntityCombatStats stats = entities.get(i);
            data[i][0] = stats.entityName;
            data[i][1] = stats.initialHealth;
            data[i][2] = stats.finalHealth;
            data[i][3] = stats.died ? "MUERTO" : "VIVO";
            data[i][4] = stats.totalDamageDealt;
            data[i][5] = stats.attacksMade;
            data[i][6] = stats.kills;
            data[i][7] = stats.damageReceived;
            
            if (stats.isDefense) {
                data[i][8] = "(" + stats.row + ", " + stats.column + ")";
            } else {
                data[i][8] = "(" + stats.finalRow + ", " + stats.finalColumn + ")";
            }
        }
        
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.setBackground(new Color(55, 55, 55));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(70, 70, 70));
        table.getTableHeader().setBackground(new Color(33, 33, 33));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(90);  // Initial Health
        table.getColumnModel().getColumn(2).setPreferredWidth(90);  // Final Health
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Damage Dealt
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Attacks
        table.getColumnModel().getColumn(6).setPreferredWidth(60);  // Kills
        table.getColumnModel().getColumn(7).setPreferredWidth(110); // Damage Received
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Position
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Summary stats
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        summaryPanel.setBackground(new Color(33, 33, 33));
        
        int totalEntities = entities.size();
        int alive = 0;
        int dead = 0;
        int totalDamageDealt = 0;
        int totalKills = 0;
        
        for (CombatLog.EntityCombatStats stats : entities) {
            if (stats.died) dead++;
            else alive++;
            totalDamageDealt += stats.totalDamageDealt;
            totalKills += stats.kills;
        }
        
        addSummaryLabel(summaryPanel, "Total:", String.valueOf(totalEntities), Color.WHITE);
        addSummaryLabel(summaryPanel, "Vivos:", String.valueOf(alive), new Color(76, 175, 80));
        addSummaryLabel(summaryPanel, "Muertos:", String.valueOf(dead), new Color(244, 67, 54));
        addSummaryLabel(summaryPanel, "Daño Total:", String.valueOf(totalDamageDealt), new Color(255, 152, 0));
        addSummaryLabel(summaryPanel, "Kills Totales:", String.valueOf(totalKills), new Color(255, 87, 34));
        
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCombatLogPanel(CombatLog log) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        textArea.setBackground(new Color(33, 33, 33));
        textArea.setForeground(Color.WHITE);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== REGISTRO DE COMBATE - NIVEL ").append(log.getLevel()).append(" ===\n\n");
        
        for (CombatLog.CombatEvent event : log.getCombatEvents()) {
            String time = String.format("[%02d:%02d] ", event.timestamp / 60000, (event.timestamp / 1000) % 60);
            
            switch (event.type) {
                case ATTACK:
                    sb.append(time)
                      .append(event.attackerName)
                      .append(" atacó a ")
                      .append(event.targetName)
                      .append(" causando ")
                      .append(event.damage)
                      .append(" de daño (")
                      .append(event.targetHealthBefore)
                      .append(" → ")
                      .append(event.targetHealthAfter)
                      .append(" HP)\n");
                    break;
                    
                case HEAL:
                    sb.append(time)
                      .append(event.attackerName)
                      .append(" curó a ")
                      .append(event.targetName)
                      .append(" por ")
                      .append(event.damage)
                      .append(" HP (")
                      .append(event.targetHealthBefore)
                      .append(" → ")
                      .append(event.targetHealthAfter)
                      .append(" HP)\n");
                    break;
                    
                case EXPLOSION:
                    sb.append(time)
                      .append("💥 ")
                      .append(event.attackerName)
                      .append(" EXPLOTÓ dañando a ")
                      .append(event.targetName)
                      .append(" por ")
                      .append(event.damage)
                      .append(" HP (")
                      .append(event.targetHealthBefore)
                      .append(" → ")
                      .append(event.targetHealthAfter)
                      .append(" HP)\n");
                    break;
                    
                case DEATH:
                    sb.append(time)
                      .append("☠ ")
                      .append(event.targetName)
                      .append(" ha MUERTO");
                    if (event.attackerName != null) {
                        sb.append(" (asesinado por ").append(event.attackerName).append(")");
                    }
                    sb.append("\n");
                    break;
            }
        }
        
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addSummaryLabel(JPanel panel, String label, String value, Color valueColor) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComponent.setForeground(Color.LIGHT_GRAY);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.BOLD, 14));
        valueComponent.setForeground(valueColor);
        
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
    
    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    /**
     * Static method to show the battle summary dialog
     */
    public static void showSummary(JFrame parent, CombatLog log, boolean victory) {
        System.out.println(">>> BattleSummaryDialog.showSummary called, victory=" + victory + ", isDialogOpen=" + isDialogOpen);
        
        // Prevent duplicate dialogs
        if (isDialogOpen) {
            System.out.println("⚠ Dialog already open, skipping duplicate");
            return;
        }
        
        isDialogOpen = true;
        System.out.println("✓ Setting isDialogOpen = true, creating dialog");
        
        try {
            BattleSummaryDialog dialog = new BattleSummaryDialog(parent, log, victory);
            System.out.println("✓ Dialog created, making visible (MODAL - will block until closed)");
            dialog.setVisible(true);
            System.out.println("✓ Dialog closed by user");
        } finally {
            isDialogOpen = false;
            System.out.println("✓ Setting isDialogOpen = false");
        }
    }
}
