package com.abstractprogrammer.nullnotion.util;

import com.abstractprogrammer.nullnotion.component.ConnectionSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseHelper {
    public static Optional<Connection> getDatabaseDriver(Project project) throws SQLException {
        ConnectionSettings connectionSettings = project.getService(ConnectionSettings.class);
        String connectionString;
        ConnectionSettings.State settingsState = connectionSettings.getState();
        if (settingsState != null) {
            if (settingsState.connectionString == null) {
                connectionString = Messages.showInputDialog(project, "Please enter the connection string for the database:", "Connection String", Messages.getQuestionIcon());
                connectionString = CommonUtil.sanitize(connectionString);
                settingsState.connectionString = connectionString;
            } else {
                connectionString = settingsState.connectionString;
            }
            String JDBC_DRIVER = null;
            if (settingsState.databaseName != null) {
                JDBC_DRIVER = settingsState.databaseName;
            } else {
                String[] options = {"MySQL", "PostgreSQL", "Oracle", "MSSQL"};
                String selectedOption = Messages.showEditableChooseDialog("Please select the database type:", "Database Type", Messages.getQuestionIcon(), options, options[0], null);
                String CONNECTION_TEMPLATE;
                if (selectedOption != null) {
                    switch (selectedOption) {
                        case "MySQL":
                            JDBC_DRIVER = "com.mysql.jdbc.Driver";
                            CONNECTION_TEMPLATE = "jdbc:mysql://<hostname>:<port>/<database>?user=<username>&password=<password>";
                            break;
                        case "PostgreSQL":
                            JDBC_DRIVER = "org.postgresql.Driver";
                            CONNECTION_TEMPLATE = "jdbc:postgresql://<hostname>:<port>/<database>?user=<username>&password=<password>";
                            break;
                        case "Oracle":
                            JDBC_DRIVER = "oracle.jdbc.OracleDriver";
                            CONNECTION_TEMPLATE = "jdbc:oracle:thin:@<hostname>:<port>:<SID>";
                            break;
                        default:
                            JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                            CONNECTION_TEMPLATE = "jdbc:sqlserver://<hostname>:<port>;database=<database>;user=<username>;password=<password>";
                            break;
                    }
                    settingsState.databaseName = JDBC_DRIVER;
                }
            }
            if (StringUtils.isNotBlank(connectionString) && StringUtils.isNotBlank(JDBC_DRIVER)) {
                try {
                    Class.forName(JDBC_DRIVER);
                } catch (ClassNotFoundException e) {
                    settingsState.databaseName = null;
                }
                return Optional.of(DriverManager.getConnection(connectionString));
            }
        }
        return Optional.empty();
    }
}
