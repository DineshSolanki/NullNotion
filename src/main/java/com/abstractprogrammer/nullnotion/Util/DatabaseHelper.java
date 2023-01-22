package com.abstractprogrammer.nullnotion.Util;

import com.abstractprogrammer.nullnotion.component.ConnectionSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseHelper {
    public static Optional<Connection> getDatabaseDriver(Project project) throws ClassNotFoundException, SQLException {
        ConnectionSettings connectionSettings = project.getService(ConnectionSettings.class);
        @Nullable String connectionString;
        if (connectionSettings.getState().connectionString == null) {
            connectionString = Messages.showInputDialog(project, "Please enter the connection string for the database:", "Connection String", Messages.getQuestionIcon());
            connectionSettings.getState().connectionString = connectionString;
        } else {
            connectionString = connectionSettings.getState().connectionString;
        }
        String JDBC_DRIVER = null;
        if(connectionSettings.getState().databaseName != null) {
            JDBC_DRIVER = connectionSettings.getState().databaseName;
        } else {
            String[] options = {"MySQL", "PostgreSQL", "Oracle", "MSSQL"};
            String selectedOption = Messages.showEditableChooseDialog( "Please select the database type:", "Database Type", Messages.getQuestionIcon(),options, options[0], null);
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
                connectionSettings.getState().databaseName = JDBC_DRIVER;
            }
        }
        if(StringUtils.isNotBlank(connectionString) && StringUtils.isNotBlank(JDBC_DRIVER)) {
            try {
                Class.forName(JDBC_DRIVER);
            } catch (ClassNotFoundException e) {
                connectionSettings.getState().databaseName = null;
            }
            return Optional.of(DriverManager.getConnection(connectionString));
        }
        return Optional.empty();
    }
}
