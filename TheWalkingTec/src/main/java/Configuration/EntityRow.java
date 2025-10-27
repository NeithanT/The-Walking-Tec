package Configuration;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class EntityRow extends JPanel {
    
    JLabel label;
    JTextField textField;
    private final int FIELD_COLUMNS = 15;
    private static final Color BG_COLOR = new Color(245, 245, 245);
    
    public EntityRow(String aspect) {
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(BG_COLOR);
        
        label = new JLabel(aspect);
        createTextField();
        
        add(label);
        add(textField);
        
    }
    
    public EntityRow(String aspect, String field) {
        this(aspect);
        
    }
    
    public void createTextField() {
    
        textField = new JTextField(FIELD_COLUMNS);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    public JTextField getTextField() {
        return textField;
    }
    
    public JLabel getLabel() {
        return label;
    }

    public void addDocumentListener(DocumentListener listener) {
        textField.getDocument().addDocumentListener(listener);
    }
    
    /**
     * Configura este campo para aceptar solo números enteros
     */
    public void setNumericOnly() {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new NumericDocumentFilter(false));
    }
    
    /**
     * Configura este campo para aceptar solo números decimales
     */
    public void setDecimalOnly() {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new NumericDocumentFilter(true));
    }
    
    /**
     * Filtro de documento que solo permite entrada numérica
     */
    private static class NumericDocumentFilter extends DocumentFilter {
        private final boolean allowDecimal;
        
        public NumericDocumentFilter(boolean allowDecimal) {
            this.allowDecimal = allowDecimal;
        }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                throws BadLocationException {
            if (isValid(string, fb.getDocument().getText(0, fb.getDocument().getLength()), offset)) {
                super.insertString(fb, offset, string, attr);
            }
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + 
                           currentText.substring(offset + length);
            
            if (isValid(text, currentText, offset)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
        
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }
        
        private boolean isValid(String input, String currentText, int offset) {
            if (input == null || input.isEmpty()) {
                return true;
            }
            
            // Construir el texto resultante
            String resultText = currentText.substring(0, offset) + input + 
                              currentText.substring(offset);
            
            // Permitir signo negativo al inicio
            if (resultText.equals("-")) {
                return true;
            }
            
            // Validar formato numérico
            try {
                if (allowDecimal) {
                    // Permitir punto decimal
                    if (resultText.matches("-?\\d*\\.?\\d*")) {
                        // No permitir múltiples puntos
                        return resultText.indexOf('.') == resultText.lastIndexOf('.');
                    }
                    return false;
                } else {
                    // Solo enteros
                    return resultText.matches("-?\\d*");
                }
            } catch (Exception e) {
                return false;
            }
        }
    }
}