/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group.thewalkingtec.Zombies;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class PlantsVsZombiesGame extends JFrame {
    private GamePanel gamePanel;
    
    public PlantsVsZombiesGame() {
        setTitle("Plants vs Zombies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlantsVsZombiesGame());
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;
    private static final int ROWS = 5;
    private static final int COLS = 9;
    private static final int CELL_SIZE = 80;
    private static final int GRID_X = 150;
    private static final int GRID_Y = 80;
    
    private Timer timer;
    private List<Plant> plants;
    private List<Zombie> zombies;
    private List<Projectile> projectiles;
    private int sun;
    private int selectedPlant;
    private Random rand;
    private int zombieSpawnCounter;
    private boolean gameOver;
    private boolean victory;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(100, 200, 100));
        
        plants = new ArrayList<>();
        zombies = new ArrayList<>();
        projectiles = new ArrayList<>();
        sun = 250;
        selectedPlant = -1;
        rand = new Random();
        zombieSpawnCounter = 0;
        gameOver = false;
        victory = false;
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        
        timer = new Timer(30, this);
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw grid
        g2d.setColor(new Color(80, 160, 80));
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.fillRect(GRID_X + col * CELL_SIZE, GRID_Y + row * CELL_SIZE, 
                           CELL_SIZE - 2, CELL_SIZE - 2);
            }
        }
        
        // Draw plant selection panel
        drawPlantSelection(g2d);
        
        // Draw sun counter
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(10, 10, 40, 40);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(sun + "", 60, 35);
        
        // Draw projectiles
        for (Projectile p : projectiles) {
            p.draw(g2d);
        }
        
        // Draw plants
        for (Plant p : plants) {
            p.draw(g2d);
        }
        
        // Draw zombies
        for (Zombie z : zombies) {
            z.draw(g2d);
        }
        
        // Draw game over or victory
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            g2d.drawString("GAME OVER!", WIDTH/2 - 180, HEIGHT/2);
        } else if (victory) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            g2d.drawString("VICTORY!", WIDTH/2 - 150, HEIGHT/2);
        }
    }
    
    private void drawPlantSelection(Graphics2D g2d) {
        // Peashooter (100 sun)
        drawPlantCard(g2d, 0, 10, 60, Color.GREEN, "Pea", 100);
        
        // Sunflower (50 sun)
        drawPlantCard(g2d, 1, 10, 150, Color.YELLOW, "Sun", 50);
        
        // Wall-nut (50 sun)
        drawPlantCard(g2d, 2, 10, 240, new Color(139, 69, 19), "Wall", 50);
    }
    
    private void drawPlantCard(Graphics2D g2d, int type, int x, int y, Color color, String name, int cost) {
        if (selectedPlant == type) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(x - 2, y - 2, 84, 84);
        }
        
        g2d.setColor(color);
        g2d.fillRect(x, y, 80, 80);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, 80, 80);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(name, x + 20, y + 40);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString(cost + " sun", x + 15, y + 70);
    }
    
    private void handleClick(int x, int y) {
        if (gameOver || victory) return;
        
        // Check plant selection
        if (x >= 10 && x <= 90) {
            if (y >= 60 && y <= 140 && sun >= 100) selectedPlant = 0;
            else if (y >= 150 && y <= 230 && sun >= 50) selectedPlant = 1;
            else if (y >= 240 && y <= 320 && sun >= 50) selectedPlant = 2;
            return;
        }
        
        // Check grid placement
        if (selectedPlant >= 0 && x >= GRID_X && x < GRID_X + COLS * CELL_SIZE &&
            y >= GRID_Y && y < GRID_Y + ROWS * CELL_SIZE) {
            
            int col = (x - GRID_X) / CELL_SIZE;
            int row = (y - GRID_Y) / CELL_SIZE;
            
            // Check if cell is empty
            boolean empty = true;
            for (Plant p : plants) {
                if (p.row == row && p.col == col) {
                    empty = false;
                    break;
                }
            }
            
            if (empty) {
                int cost = selectedPlant == 0 ? 100 : 50;
                if (sun >= cost) {
                    plants.add(new Plant(row, col, selectedPlant));
                    sun -= cost;
                    selectedPlant = -1;
                }
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || victory) return;
        
        zombieSpawnCounter++;
        if (zombieSpawnCounter >= 120) {
            zombieSpawnCounter = 0;
            int row = rand.nextInt(ROWS);
            zombies.add(new Zombie(row));
        }
        
        // Update plants
        for (Plant p : plants) {
            p.update();
            if (p.type == 0) { // Peashooter
                if (p.shootCounter >= 60) {
                    boolean zombieInRow = false;
                    for (Zombie z : zombies) {
                        if (z.row == p.row && z.x > p.x) {
                            zombieInRow = true;
                            break;
                        }
                    }
                    if (zombieInRow) {
                        projectiles.add(new Projectile(p.x + 30, p.y + 20, p.row));
                        p.shootCounter = 0;
                    }
                }
            } else if (p.type == 1) { // Sunflower
                if (p.shootCounter >= 180) {
                    sun += 25;
                    p.shootCounter = 0;
                }
            }
        }
        
        // Update zombies
        List<Zombie> zombiesToRemove = new ArrayList<>();
        for (Zombie z : zombies) {
            z.update();
            
            // Check collision with plants
            Plant targetPlant = null;
            for (Plant p : plants) {
                if (p.row == z.row && Math.abs(p.x - z.x) < 40) {
                    targetPlant = p;
                    break;
                }
            }
            
            if (targetPlant != null) {
                z.attacking = true;
                targetPlant.hp -= 1;
                if (targetPlant.hp <= 0) {
                    plants.remove(targetPlant);
                }
            } else {
                z.attacking = false;
            }
            
            if (z.hp <= 0) {
                zombiesToRemove.add(z);
            }
            
            if (z.x < 100) {
                gameOver = true;
            }
        }
        zombies.removeAll(zombiesToRemove);
        
        // Update projectiles
        List<Projectile> projectilesToRemove = new ArrayList<>();
        for (Projectile p : projectiles) {
            p.update();
            
            // Check collision with zombies
            for (Zombie z : zombies) {
                if (z.row == p.row && Math.abs(z.x - p.x) < 30) {
                    z.hp -= 30;
                    projectilesToRemove.add(p);
                    break;
                }
            }
            
            if (p.x > WIDTH) {
                projectilesToRemove.add(p);
            }
        }
        projectiles.removeAll(projectilesToRemove);
        
        // Check victory (survived long enough)
        if (zombieSpawnCounter == 0 && zombies.isEmpty() && plants.size() > 3) {
            victory = true;
        }
        
        repaint();
    }
    
    class Plant {
        int row, col, type;
        int x, y;
        int hp;
        int shootCounter;
        
        Plant(int row, int col, int type) {
            this.row = row;
            this.col = col;
            this.type = type;
            this.x = GRID_X + col * CELL_SIZE + 10;
            this.y = GRID_Y + row * CELL_SIZE + 10;
            this.hp = type == 2 ? 300 : 100; // Wall-nut has more HP
            this.shootCounter = 0;
        }
        
        void update() {
            shootCounter++;
        }
        
        void draw(Graphics2D g2d) {
            if (type == 0) { // Peashooter
                g2d.setColor(Color.GREEN);
                g2d.fillOval(x, y, 60, 60);
                g2d.setColor(new Color(0, 100, 0));
                g2d.fillRect(x + 50, y + 25, 15, 10);
            } else if (type == 1) { // Sunflower
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(x, y, 60, 60);
                g2d.setColor(Color.ORANGE);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * 2 * i / 8;
                    int px = (int)(x + 30 + Math.cos(angle) * 25);
                    int py = (int)(y + 30 + Math.sin(angle) * 25);
                    g2d.fillOval(px - 5, py - 5, 10, 10);
                }
            } else if (type == 2) { // Wall-nut
                g2d.setColor(new Color(139, 69, 19));
                g2d.fillOval(x, y, 60, 60);
                g2d.setColor(new Color(101, 67, 33));
                g2d.fillOval(x + 15, y + 15, 30, 30);
            }
            
            // HP bar
            g2d.setColor(Color.RED);
            g2d.fillRect(x, y - 10, 60, 5);
            g2d.setColor(Color.GREEN);
            int maxHp = type == 2 ? 300 : 100;
            g2d.fillRect(x, y - 10, 60 * hp / maxHp, 5);
        }
    }
    
    class Zombie {
        int row;
        int x, y;
        int hp;
        boolean attacking;
        
        Zombie(int row) {
            this.row = row;
            this.x = WIDTH - 50;
            this.y = GRID_Y + row * CELL_SIZE + 10;
            this.hp = 100;
            this.attacking = false;
        }
        
        void update() {
            if (!attacking) {
                x -= 1;
            }
        }
        
        void draw(Graphics2D g2d) {
            g2d.setColor(new Color(100, 150, 100));
            g2d.fillOval(x, y, 60, 60);
            
            // Eyes
            g2d.setColor(Color.RED);
            g2d.fillOval(x + 15, y + 20, 10, 10);
            g2d.fillOval(x + 35, y + 20, 10, 10);
            
            // Mouth
            g2d.setColor(Color.BLACK);
            g2d.drawArc(x + 15, y + 30, 30, 20, 0, -180);
            
            // HP bar
            g2d.setColor(Color.RED);
            g2d.fillRect(x, y - 10, 60, 5);
            g2d.setColor(Color.GREEN);
            g2d.fillRect(x, y - 10, 60 * hp / 100, 5);
        }
    }
    
    class Projectile {
        int x, y, row;
        
        Projectile(int x, int y, int row) {
            this.x = x;
            this.y = y;
            this.row = row;
        }
        
        void update() {
            x += 5;
        }
        
        void draw(Graphics2D g2d) {
            g2d.setColor(Color.GREEN);
            g2d.fillOval(x, y, 15, 15);
        }
    }
}