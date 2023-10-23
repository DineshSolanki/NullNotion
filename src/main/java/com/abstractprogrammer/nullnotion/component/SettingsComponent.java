package com.abstractprogrammer.nullnotion.component;

import com.abstractprogrammer.nullnotion.enums.AuthenticationMode;
import com.abstractprogrammer.nullnotion.enums.DatabaseType;
import com.abstractprogrammer.nullnotion.model.DatabaseConnection;
import com.abstractprogrammer.nullnotion.service.SettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SettingsComponent {
    private final JPanel mainPanel;

    private final JTextField hostTxt = new JBTextField();
    private final JTextField portTxt = new JBTextField();
    private final JTextField userTxt = new JBTextField();
    private final JBPasswordField passwordTxt = new JBPasswordField();
    private final ComboBox<DatabaseType> databaseTypeComboBox = new ComboBox<>(DatabaseType.values());

    private final JTextField databaseTxt = new JBTextField();
    private final JButton testConnectionBtn = new JButton("Test Connection");

    public final ComboBox<AuthenticationMode> authenticationModeComboBox = new ComboBox<>(AuthenticationMode.values());
    public JPanel authenticationPanel = new JPanel();


    public SettingsComponent() {
        authenticationPanel.setLayout(new BoxLayout(authenticationPanel, BoxLayout.X_AXIS));
        authenticationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel hostPortPanel = new JPanel();
        hostPortPanel.setLayout(new BoxLayout(hostPortPanel, BoxLayout.X_AXIS));
        hostPortPanel.add(new JBLabel("Host "));
        hostPortPanel.add(hostTxt);
        hostPortPanel.add(Box.createHorizontalStrut(10));
        hostPortPanel.add(new JBLabel("Port "));
        hostPortPanel.add(portTxt);

        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(hostPortPanel)
                .addLabeledComponent(new JBLabel("Database"), databaseTxt, 1)
                .addLabeledComponent(new JBLabel("Type"), databaseTypeComboBox, 1)
                .addLabeledComponent(new JBLabel("Authentication"), authenticationModeComboBox, 1)
                .addComponent(authenticationPanel)
                .addComponent(testConnectionBtn)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        testConnectionBtn.addActionListener(e -> {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                DatabaseConnection databaseConnection = getDatabaseConnection();

                boolean result = databaseConnection.testConnection();
                if (result) {
                    ApplicationManager.getApplication().invokeAndWait(() -> Messages.showInfoMessage("Connection successful",
                            "Connection Test"), ModalityState.any());

                } else {
                    ApplicationManager.getApplication().invokeAndWait(() ->
                    Messages.showErrorDialog("Connection failed",
                            "Connection Test"), ModalityState.any());
                }
            }, "Testing connection...", false, null);
        });
        authenticationModeComboBox.addActionListener(e -> {
            AuthenticationMode mode = (AuthenticationMode) authenticationModeComboBox.getSelectedItem();
            if (mode != null) {
                authenticationPanel.removeAll();
                authenticationPanel.add(createAuthenticationPanel(mode));
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }

    @NotNull
    private DatabaseConnection getDatabaseConnection() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.setHost(hostTxt.getText());
        databaseConnection.setPort(portTxt.getText());
        databaseConnection.setDatabaseType((DatabaseType) databaseTypeComboBox.getSelectedItem());
        databaseConnection.setAuthenticationMode((AuthenticationMode) authenticationModeComboBox.getSelectedItem());
        databaseConnection.setUsername(userTxt.getText());
        databaseConnection.setPassword(String.valueOf(passwordTxt.getPassword()));
        databaseConnection.setDatabaseName(databaseTxt.getText());
        return databaseConnection;
    }

    private JPanel createAuthenticationPanel(AuthenticationMode mode) {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();
        switch (mode) {
            case NONE:
                return formBuilder.getPanel();
            case USER:
                return formBuilder
                        .addLabeledComponent(new JBLabel("Username"),
                                userTxt,
                                1,
                                false)
                        .addComponentFillVertically(new JPanel(), 0)
                        .getPanel();
            case USER_PASSWORD:
                return formBuilder
                        .addLabeledComponent(new JBLabel("Username"),
                                userTxt,
                                1,
                                false)
                        .addLabeledComponent(new JBLabel("Password"),
                                passwordTxt,
                                1,
                                false)
                        .addComponentFillVertically(new JPanel(), 0)
                        .getPanel();
            case OS_CREDENTIALS:
                return formBuilder.getPanel();
            default:
                return formBuilder.getPanel();
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return hostTxt;
    }

    public String getHostText() {
        return hostTxt.getText();
    }

    public String getPortText() {
        return portTxt.getText();
    }

    public String getDatabaseText() {
        return databaseTxt.getText();
    }

    public DatabaseType getDatabaseType() {
        return (DatabaseType) databaseTypeComboBox.getSelectedItem();
    }

    public void setHostText(String host) {
        hostTxt.setText(host);
    }

    public void setPortText(String port) {
        portTxt.setText(port);
    }

    public void setDatabaseText(String database) {
        databaseTxt.setText(database);
    }

    public void setDatabaseType(DatabaseType databaseType) {
        databaseTypeComboBox.setSelectedItem(databaseType);
    }

    public SettingsState getSettingsState() {
        SettingsState settingsState = SettingsState.getInstance();
        settingsState.host = hostTxt.getText();
        settingsState.port = portTxt.getText();
        settingsState.authenticationMode = (AuthenticationMode) authenticationModeComboBox.getSelectedItem();
        settingsState.username = userTxt.getText();
        settingsState.password = String.valueOf(passwordTxt.getPassword());
        settingsState.databaseName = databaseTxt.getText();
        settingsState.databaseType = (DatabaseType) databaseTypeComboBox.getSelectedItem();
        return settingsState;
    }
    public void setSettingsState(SettingsState settingsState) {
        hostTxt.setText(settingsState.host);
        portTxt.setText(settingsState.port);
        authenticationModeComboBox.setSelectedItem(settingsState.authenticationMode);
        userTxt.setText(settingsState.username);
        passwordTxt.setText(settingsState.password);
        databaseTxt.setText(settingsState.databaseName);
        databaseTypeComboBox.setSelectedItem(settingsState.databaseType);
    }
}
