package com.abstractprogrammer.nullnotion.enums;

import java.util.Arrays;

public enum DatabaseType {
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;database=%s"),
    MySQL("com.mysql.jdbc.Driver", "jdbc:mysql://%s:%s/%s"),
    PostgreSQL("org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s"),
    Oracle("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s");

    private final String driver;
    private final String connectionFormat;

    DatabaseType(String driver, String connectionFormat) {
        this.driver = driver;
        this.connectionFormat = connectionFormat;
    }

    public String getDriver() {
        return driver;
    }
    public String getConnectionFormat() {
        return connectionFormat;
    }
    // Get all the values of the enum as String array
    public static String[] getValues() {
        DatabaseType[] values = DatabaseType.values();
        return Arrays.stream(values).map(Enum::name).toArray(String[]::new);
    }
    
}
