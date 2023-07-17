package com.abstractprogrammer.nullnotion.model;

import com.abstractprogrammer.nullnotion.enums.AuthenticationMode;
import com.abstractprogrammer.nullnotion.enums.DatabaseType;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private String connectionString;
    private DatabaseType databaseType;
    private String host;
    private String port;
    private AuthenticationMode authenticationMode;
    private String username;
    private String password;
    private String databaseName;

    public DatabaseConnection(String connectionString, DatabaseType databaseName) {
        this.connectionString = connectionString;
        this.databaseType = databaseName;
    }
    public DatabaseConnection(){}
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(String port) { 
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setAuthenticationMode(AuthenticationMode authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public AuthenticationMode getAuthenticationMode() {
        return authenticationMode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean testConnection() {
        connectionString = buildConnectionString();
        boolean result = false;
        try {
            // Load the JDBC driver based on the database type
            Class.forName(databaseType.getDriver());

            Connection connection;
            switch(authenticationMode) {
                case USER:
                    if (username == null) {
                        throw new IllegalArgumentException("Username must be set for USER mode authentication");
                    }
                    connection = DriverManager.getConnection(connectionString, username, "");
                    break;

                case USER_PASSWORD:
                    if (username == null || password == null) {
                        throw new IllegalArgumentException("Username and password must be set for USER_PASSWORD mode authentication");
                    }
                    connection = DriverManager.getConnection(connectionString, username, password);
                    break;

                case OS_CREDENTIALS:
                case NONE:
                default:
                    connection = DriverManager.getConnection(connectionString);
            }

            if (connection != null) {
                result = true;
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public String buildConnectionString() {
        if (databaseType == null || host == null || port == null || databaseName == null) {
            throw new IllegalArgumentException("All parameters required for connection string are not set");
        }
        this.connectionString = String.format(databaseType.getConnectionFormat(), host, port, databaseName);
        return connectionString;
    }
}
