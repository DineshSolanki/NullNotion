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
}
