package Configuration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class AdminManagementDialog extends JDialog {

    private final ConfigManager configManager;
    private final DefaultListModel<Admin> adminModel;
    private final JList<Admin> adminList;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public AdminManagementDialog(java.awt.Frame owner, ConfigManager manager) {
        super(owner, "Administradores", true);
        this.configManager = manager;
        this.adminModel = new DefaultListModel<>();
        this.adminList = new JList<>(adminModel);
        this.usernameField = new JTextField(18);
        this.passwordField = new JPasswordField(18);

        setPreferredSize(new Dimension(460, 320));
        setMinimumSize(new Dimension(420, 280));

        initializeLayout();
        loadAdmins();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeLayout() {
        setLayout(new BorderLayout(12, 12));
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JScrollPane listScroll = new JScrollPane(adminList);
        listScroll.setPreferredSize(new Dimension(180, 220));
        adminList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });
        content.add(listScroll, BorderLayout.WEST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;

        formPanel.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Agregar");
        addButton.addActionListener(this::handleAdd);
        JButton updateButton = new JButton("Actualizar");
        updateButton.addActionListener(this::handleUpdate);
        JButton deleteButton = new JButton("Eliminar");
        deleteButton.addActionListener(this::handleDelete);
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        content.add(formPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private void loadAdmins() {
        adminModel.clear();
        for (Admin admin : configManager.getAdmins()) {
            adminModel.addElement(admin);
        }
        if (!adminModel.isEmpty()) {
            adminList.setSelectedIndex(0);
        }
    }

    private void populateFieldsFromSelection() {
        Admin selected = adminList.getSelectedValue();
        if (selected != null) {
            usernameField.setText(selected.getUsername());
            passwordField.setText(selected.getPassword());
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        adminList.clearSelection();
    }

    private void handleAdd(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!validateFields(username, password)) {
            return;
        }

        boolean success = configManager.addAdmin(new Admin(username, password));
        if (!success) {
            JOptionPane.showMessageDialog(this, "No se pudo agregar. Verifique que el usuario sea único.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadAdmins();
        selectAdminByUsername(username);
    }

    private void handleUpdate(ActionEvent event) {
        Admin selected = adminList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un administrador para actualizar.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!validateFields(username, password)) {
            return;
        }

        boolean success = configManager.updateAdmin(selected.getUsername(), new Admin(username, password));
        if (!success) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar. Verifique que el usuario sea único.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadAdmins();
        selectAdminByUsername(username);
    }

    private void handleDelete(ActionEvent event) {
        Admin selected = adminList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un administrador para eliminar.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al administrador '" + selected.getUsername() + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            configManager.removeAdmin(selected);
            loadAdmins();
            clearFields();
        }
    }

    private boolean validateFields(String username, String password) {
        if (username.isBlank()) {
            JOptionPane.showMessageDialog(this, "El usuario no puede estar vacío.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (password.isBlank()) {
            JOptionPane.showMessageDialog(this, "La contraseña no puede estar vacía.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void selectAdminByUsername(String username) {
        for (int i = 0; i < adminModel.size(); i++) {
            Admin admin = adminModel.get(i);
            if (admin.getUsername().equalsIgnoreCase(username)) {
                adminList.setSelectedIndex(i);
                adminList.ensureIndexIsVisible(i);
                break;
            }
        }
    }
}
