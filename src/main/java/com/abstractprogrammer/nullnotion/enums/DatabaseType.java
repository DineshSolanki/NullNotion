package com.abstractprogrammer.nullnotion.enums;

import java.util.Arrays;

public enum DatabaseType {
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    MySQL("com.mysql.jdbc.Driver"),
    PostgreSQL("org.postgresql.Driver"),
    Oracle("oracle.jdbc.OracleDriver");

    private final String driver;

    DatabaseType(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
    // Get all the values of the enum as String array
    public static String[] getValues() {
        DatabaseType[] values = DatabaseType.values();
        return Arrays.stream(values).map(Enum::name).toArray(String[]::new);
    }
    
}
