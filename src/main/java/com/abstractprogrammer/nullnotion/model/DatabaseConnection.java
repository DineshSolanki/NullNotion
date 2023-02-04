package com.abstractprogrammer.nullnotion.model;

import com.abstractprogrammer.nullnotion.enums.DatabaseType;

public class DatabaseConnection {
    private String connectionString;
    private DatabaseType databaseType;
    
    public DatabaseConnection(String connectionString, DatabaseType databaseName) {
        this.connectionString = connectionString;
        this.databaseType = databaseName;
    }

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
}
