package com.abstractprogrammer.nullnotion.util;

public class CommonUtil {
    public static String sanitize(String input) {
        // Remove any unwanted characters or whitespace
        input = input.trim().replaceAll("[^a-zA-Z0-9\\-_.]", "");
        // Escape any special characters that can be used in SQL injection
        input = input.replaceAll("'", "''");
        return input;
    }
    private boolean isValidConnectionString(String connectionString, String databaseType) {
        return switch (databaseType) {
            case "MySQL" ->
                    connectionString.matches("jdbc:mysql://[\\w\\d.]+:[\\d]+/[\\w\\d]+\\?user=[\\w\\d]+&password=[\\w\\d]+");
            case "PostgreSQL" ->
                    connectionString.matches("jdbc:postgresql://[\\w\\d\\.]+:[\\d]+/[\\w\\d]+\\?user=[\\w\\d]+&password=[\\w\\d]+");
            case "Oracle" -> connectionString.matches("jdbc:oracle:thin:@[\\w\\d\\.]+:[\\d]+:[\\w\\d]+");
            case "MSSQL" ->
                    connectionString.matches("jdbc:sqlserver://[\\w\\d\\.]+:[\\d]+;database=[\\w\\d]+;user=[\\w\\d]+;password=[\\w\\d]+");
            default -> false;
        };
    }
}
