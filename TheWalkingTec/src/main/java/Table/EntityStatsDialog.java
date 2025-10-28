package Table;

import GameLogic.CombatLog;
import Entity.Entity;
import Defense.Defense;
import Zombie.Zombie;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * EntityStatsDialog - Shows detailed combat statistics for an entity when clicked
 */
public class EntityStatsDialog extends JDialog {
    
    private static boolean isDialogOpen = false;
    private CombatLog combatLog; // Store combat log for later use
    
    public EntityStatsDialog(JFrame parent, Entity entity, CombatLog.EntityCombatStats stats, CombatLog log) {
        super(parent, "Estadísticas de " + entity.getEntityName(), true);
        this.combatLog = log;
        
        setLayout(new BorderLayout(10, 10));
        setSize(500, 600);
        setLocationRelativeTo(parent);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(45, 45, 45));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(33, 33, 33));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(entity.getEntityName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        JLabel typeLabel = new JLabel(entity instanceof Defense ? "DEFENSA" : "ZOMBIE");
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        typeLabel.setForeground(entity instanceof Defense ? new Color(76, 175, 80) : new Color(244, 67, 54));
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(typeLabel);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(45, 45, 45));
        
        // Health information
        addSection(statsPanel, "INFORMACIÓN DE VIDA");
        addStat(statsPanel, "Vida Inicial:", stats.initialHealth + " HP", new Color(76, 175, 80));
        addStat(statsPanel, "Vida Final:", stats.finalHealth + " HP", stats.died ? new Color(244, 67, 54) : new Color(76, 175, 80));
        addStat(statsPanel, "Estado:", stats.died ? "MUERTO" : "VIVO", stats.died ? new Color(244, 67, 54) : new Color(76, 175, 80));
        
        if (stats.died && stats.killedBy != null) {
            // Find killer name from log
            String killerName = findEntityName(stats.killedBy, log);
            addStat(statsPanel, "Asesinado por:", killerName, new Color(255, 152, 0));
        }
        
        statsPanel.add(Box.createVerticalStrut(10));
        
        // Position information
        addSection(statsPanel, "POSICIÓN");
        if (entity instanceof Defense) {
            addStat(statsPanel, "Posición:", "(" + stats.row + ", " + stats.column + ")", new Color(33, 150, 243));
        } else {
            addStat(statsPanel, "Posición Inicial:", "Borde del mapa", new Color(33, 150, 243));
            addStat(statsPanel, "Posición Final:", "(" + stats.finalRow + ", " + stats.finalColumn + ")", new Color(33, 150, 243));
        }
        
        statsPanel.add(Box.createVerticalStrut(10));
        
        // Combat statistics
        addSection(statsPanel, "ESTADÍSTICAS DE COMBATE");
        
        if (stats.attacksMade > 0 || stats.totalDamageDealt > 0) {
            addStat(statsPanel, "Ataques Realizados:", String.valueOf(stats.attacksMade), new Color(244, 67, 54));
            addStat(statsPanel, "Daño Total Causado:", stats.totalDamageDealt + " HP", new Color(244, 67, 54));
            addStat(statsPanel, "Enemigos Atacados:", String.valueOf(stats.targetsAttacked.size()), new Color(244, 67, 54));
            addStat(statsPanel, "Asesinatos:", String.valueOf(stats.kills), new Color(255, 87, 34));
            
            // Show list of attacked entities
            if (!stats.targetsAttacked.isEmpty()) {
                addEntityList(statsPanel, "A quién atacó:", stats.targetsAttacked, new Color(244, 67, 54));
            }
            
            long duration = log.getBattleDuration();
            if (duration > 0) {
                addStat(statsPanel, "Golpes por Segundo:", String.format("%.2f", stats.getHitsPerSecond(duration)), new Color(255, 152, 0));
                addStat(statsPanel, "Daño por Segundo:", String.format("%.2f", stats.getDamagePerSecond(duration)) + " HP", new Color(255, 152, 0));
            }
        }
        
        if (stats.explosions > 0) {
            addStat(statsPanel, "Explosiones:", String.valueOf(stats.explosions), new Color(255, 87, 34));
        }
        
        if (stats.healsMade > 0 || stats.totalHealingDone > 0) {
            addStat(statsPanel, "Curaciones Realizadas:", String.valueOf(stats.healsMade), new Color(76, 175, 80));
            addStat(statsPanel, "Curación Total:", stats.totalHealingDone + " HP", new Color(76, 175, 80));
            addStat(statsPanel, "Aliados Curados:", String.valueOf(stats.entitiesHealed.size()), new Color(76, 175, 80));
        }
        
        statsPanel.add(Box.createVerticalStrut(10));
        
        // Damage received
        addSection(statsPanel, "DAÑO RECIBIDO");
        addStat(statsPanel, "Ataques Recibidos:", String.valueOf(stats.attacksReceived), new Color(156, 39, 176));
        addStat(statsPanel, "Daño Total Recibido:", stats.damageReceived + " HP", new Color(156, 39, 176));
        addStat(statsPanel, "Atacado Por:", stats.attackedBy.size() + " enemigos", new Color(156, 39, 176));
        
        // Show list of attackers
        if (!stats.attackedBy.isEmpty()) {
            addEntityList(statsPanel, "Quién lo atacó:", stats.attackedBy, new Color(156, 39, 176));
        }
        
        if (stats.healingReceived > 0) {
            addStat(statsPanel, "Curación Recibida:", stats.healingReceived + " HP", new Color(76, 175, 80));
        }
        
        // Scroll pane for stats
        JScrollPane scrollPane = new JScrollPane(statsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("Cerrar");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(33, 150, 243));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(45, 45, 45));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        // Don't call setVisible here - it will be called by showStats()
    }
    
    private void addSection(JPanel panel, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(255, 193, 7));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(sectionLabel);
    }
    
    private void addStat(JPanel panel, String label, String value, Color valueColor) {
        JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        statPanel.setBackground(new Color(45, 45, 45));
        statPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComponent.setForeground(Color.LIGHT_GRAY);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.BOLD, 12));
        valueComponent.setForeground(valueColor);
        
        statPanel.add(labelComponent);
        statPanel.add(valueComponent);
        
        panel.add(statPanel);
    }
    
    /**
     * Add a list of entities (shows their display names)
     */
    private void addEntityList(JPanel panel, String label, ArrayList<String> entityKeys, Color valueColor) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25 * entityKeys.size() + 20));
        listPanel.setBackground(new Color(45, 45, 45));
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        labelComponent.setForeground(valueColor);
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        listPanel.add(labelComponent);
        listPanel.add(Box.createVerticalStrut(5));
        
        // Entity names
        for (String entityKey : entityKeys) {
            String displayName = findEntityDisplayName(entityKey);
            JLabel nameLabel = new JLabel("  • " + displayName);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            nameLabel.setForeground(Color.LIGHT_GRAY);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(nameLabel);
        }
        
        panel.add(listPanel);
        panel.add(Box.createVerticalStrut(5));
    }
    
    private String findEntityDisplayName(String entityKey) {
        CombatLog.EntityCombatStats stats = combatLog.getStatsByKey(entityKey);
        return stats != null ? stats.displayName : "Desconocido";
    }
    
    private String findEntityName(String entityKey, CombatLog log) {
        CombatLog.EntityCombatStats stats = log.getStatsByKey(entityKey);
        return stats != null ? stats.entityName : "Desconocido";
    }
    
    /**
     * Static method to show entity stats dialog
     */
    public static void showStats(JFrame parent, Entity entity, CombatLog log) {
        System.out.println(">>> EntityStatsDialog.showStats called for " + entity.getEntityName() + ", isDialogOpen=" + isDialogOpen);
        
        // Prevent duplicate dialogs
        if (isDialogOpen) {
            System.out.println("⚠ Dialog already open, skipping duplicate");
            return;
        }
        
        if (log == null) {
            System.out.println("⚠ No combat log available");
            return; // No combat log available
        }
        
        CombatLog.EntityCombatStats stats = log.getStats(entity);
        if (stats == null) {
            System.out.println("⚠ No stats for this entity");
            return; // No stats for this entity
        }
        
        isDialogOpen = true;
        System.out.println("✓ Setting isDialogOpen = true, creating dialog");
        
        try {
            EntityStatsDialog dialog = new EntityStatsDialog(parent, entity, stats, log);
            System.out.println("✓ Dialog created, making visible");
            dialog.setVisible(true);
            System.out.println("✓ Dialog closed by user");
        } finally {
            isDialogOpen = false;
            System.out.println("✓ Setting isDialogOpen = false");
        }
    }
}
