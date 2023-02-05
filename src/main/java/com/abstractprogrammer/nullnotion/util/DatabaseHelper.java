package com.abstractprogrammer.nullnotion.util;

import com.abstractprogrammer.nullnotion.service.ConnectionSettings;
import com.abstractprogrammer.nullnotion.enums.DatabaseType;
import com.abstractprogrammer.nullnotion.model.DatabaseConnection;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseHelper {
    public static Optional<DatabaseConnection> getConnectionProperties(Project project) {
        ConnectionSettings connectionSettings = project.getService(ConnectionSettings.class);
        String connectionString;
        ConnectionSettings.State settingsState = connectionSettings.getState();
        if (settingsState != null) {
            if (settingsState.connectionString != null) {
                connectionString = settingsState.connectionString;
            } else {
                connectionString = Messages.showInputDialog(project, "Please enter the connection string for the database:", "Connection String", Messages.getQuestionIcon());
                if (StringUtils.isBlank(connectionString)) {
                    return Optional.empty();
                }
            }
            String selectedOption;
            if (settingsState.databaseName != null) {
                selectedOption = settingsState.databaseName;
            } else {
                String[] options = DatabaseType.getValues();
                selectedOption = Messages.showEditableChooseDialog("Please select the database type:", "Database Type", Messages.getQuestionIcon(), options, options[0], null);
                if (StringUtils.isBlank(selectedOption)) {
                    return Optional.empty();
                }
            }
            DatabaseConnection databaseConnection = new DatabaseConnection(connectionString, DatabaseType.valueOf(selectedOption));
            return Optional.of(databaseConnection);
        }
        return Optional.empty();
    }

    public static Optional<Connection> getDatabaseDriver(Project project, DatabaseConnection databaseConnection) throws SQLException {
        ConnectionSettings connectionSettings = project.getService(ConnectionSettings.class);
        ConnectionSettings.State settingsState = connectionSettings.getState();

        String connectionString = databaseConnection.getConnectionString();
        DatabaseType selectedOption = databaseConnection.getDatabaseType();
        try {
            Class.forName(selectedOption.getDriver());
        } catch (ClassNotFoundException e) {
            if (settingsState != null) {
                settingsState.connectionString = null;
                settingsState.databaseName = null;
            }
            return Optional.empty();
        }
        if (settingsState != null) {
            settingsState.connectionString = connectionString;
            settingsState.databaseName = selectedOption.name();
        }
        return Optional.of(DriverManager.getConnection(connectionString));
    }
}
