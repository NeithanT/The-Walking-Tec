package Configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class AdminLoginDialog extends JDialog {

    private final ConfigManager configManager;
    private boolean authenticated = false;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public AdminLoginDialog(java.awt.Frame owner) {
        super(owner, "Acceso de Administrador", true);
        this.configManager = new ConfigManager();
        initializeComponents();
        pack();
        setMinimumSize(new Dimension(360, 220));
        setLocationRelativeTo(owner);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_END;

        JLabel userLabel = new JLabel("Usuario:");
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        usernameField = new JTextField(16);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel passwordLabel = new JLabel("Contraseña:");
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        passwordField = new JPasswordField(16);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton loginButton = new JButton("Ingresar");
        loginButton.addActionListener(this::handleLogin);
        formPanel.add(loginButton, gbc);
        getRootPane().setDefaultButton(loginButton);

        add(formPanel, BorderLayout.CENTER);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 16, 16));
        add(errorLabel, BorderLayout.SOUTH);

        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptAuthentication();
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);
    }

    private void handleLogin(ActionEvent event) {
        attemptAuthentication();
    }

    private void attemptAuthentication() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Ingrese usuario y contraseña.");
            return;
        }

        if (configManager.authenticateAdmin(username, password)) {
            authenticated = true;
            dispose();
        } else {
            showError("Credenciales incorrectas.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    public boolean showAndAuthenticate() {
        setVisible(true);
        return authenticated;
    }
}
