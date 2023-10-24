package com.abstractprogrammer.nullnotion.enums;

public enum AuthenticationMode {
    NONE("None"),
    USER("User"),
    USER_PASSWORD("User/Password"),
    OS_CREDENTIALS("OS Credentials");

    private final String name;

    AuthenticationMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static AuthenticationMode fromString(String name) {
        for (AuthenticationMode mode : AuthenticationMode.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }

    public static AuthenticationMode fromOrdinal(int ordinal) {
        for (AuthenticationMode mode : AuthenticationMode.values()) {
            if (mode.ordinal() == ordinal) {
                return mode;
            }
        }
        return null;
    }


}
