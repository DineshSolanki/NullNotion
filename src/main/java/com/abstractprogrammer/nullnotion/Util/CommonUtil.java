package com.abstractprogrammer.nullnotion.Util;

import org.apache.commons.lang.StringEscapeUtils;

public class CommonUtil {
    public static String sanitize(String input) {
        // Remove any unwanted characters or whitespace
        input = input.trim().replaceAll("[^a-zA-Z0-9\\-_.]", "");
        // Escape any special characters that can be used in SQL injection
        input = StringEscapeUtils.escapeSql(input);
        return input;
    }
    private boolean isValidConnectionString(String connectionString, String databaseType) {
        switch(databaseType) {
            case "MySQL":
                return connectionString.matches("jdbc:mysql://[\\w\\d.]+:[\\d]+/[\\w\\d]+\\?user=[\\w\\d]+&password=[\\w\\d]+");
            case "PostgreSQL":
                return connectionString.matches("jdbc:postgresql://[\\w\\d\\.]+:[\\d]+/[\\w\\d]+\\?user=[\\w\\d]+&password=[\\w\\d]+");
            case "Oracle":
                return connectionString.matches("jdbc:oracle:thin:@[\\w\\d\\.]+:[\\d]+:[\\w\\d]+");
            case "MSSQL":
                return connectionString.matches("jdbc:sqlserver://[\\w\\d\\.]+:[\\d]+;database=[\\w\\d]+;user=[\\w\\d]+;password=[\\w\\d]+");
            default:
                return false;
        }
    }
}
