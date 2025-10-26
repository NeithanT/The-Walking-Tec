package Vanity;

import java.awt.Color;
import java.awt.Cursor;
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
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = RoundedButton.this.hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = RoundedButton.this.normalColor;
                    repaint();
                }
            });
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fillColor = currentColor;
            if (!isEnabled()) {
                fillColor = fillColor.darker();
            } else if (getModel().isArmed() && getModel().isPressed()) {
                fillColor = fillColor.darker();
            }
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }