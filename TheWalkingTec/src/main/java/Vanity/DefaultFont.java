package Vanity;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;


public class DefaultFont {

    private static final int MIN_FONT_SIZE = 12;
    private static final int MAX_FONT_SIZE = 32;
    private static Font font = new Font("Arial", Font.PLAIN, MAX_FONT_SIZE);
    private static final Color TEXT_COLOR = Color.WHITE;

    public static void applyFontToButton(JButton button) {
        button.setFont(font);
        button.setForeground(TEXT_COLOR);
    }
}
