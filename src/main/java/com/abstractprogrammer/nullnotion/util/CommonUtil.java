package com.abstractprogrammer.nullnotion.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
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
    public static void showBalloonNotification(Project project, String message, MessageType type) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(message, type, null)
                .setFadeoutTime(7500)
                .setHideOnClickOutside(true)
                .createBalloon()
                .show(RelativePoint.getNorthEastOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

}
