package Vanity;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;


public class RoundedButton extends JButton {
    
        private final Color normalColor;
        private final Color hoverColor;
        private final int cornerRadius;
        private Color currentColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text, Color normalColor, Color hoverColor, int cornerRadius) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.cornerRadius = cornerRadius;
            this.currentColor = normalColor;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 28, 12, 28));
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    currentColor = RoundedButton.this.hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    currentColor = RoundedButton.this.normalColor;
                    repaint();
                }
                
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            Color fillColor = currentColor;
            
            if (!isEnabled()) {
                fillColor = fillColor.darker().darker();
            } else if (isPressed) {
                fillColor = fillColor.darker();
            }
            
            // Draw shadow for depth effect
            if (isEnabled() && !isPressed) {
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);
            }
            
            // Draw button background
            g2.setColor(fillColor);
            int offset = isPressed ? 2 : 0;
            g2.fillRoundRect(offset, offset, getWidth() - 2 - offset, getHeight() - 2 - offset, cornerRadius, cornerRadius);
            
            // Draw subtle highlight on top
            if (isHovered && !isPressed) {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(offset, offset, getWidth() - 2 - offset, (getHeight() - 2 - offset) / 2, cornerRadius, cornerRadius / 2);
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
    }