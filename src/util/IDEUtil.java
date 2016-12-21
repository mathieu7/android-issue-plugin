package util;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class IDEUtil {


    private static final Pattern NEWLINE_REGEX = Pattern.compile("\n", Pattern.LITERAL);

    public static void displayToolsNotification(@NotNull NotificationType type,
                                                @NotNull Project project,
                                                @NotNull String title,
                                                @NotNull String message) {
        Notifications.Bus.notify(new Notification(
                title, title,
                replaceNewlines(message) + "<br/><a href='configureAndroidIssuesPlugin'>Configure</a>",
                type, new AndroidIssuesNotificationListener(project)), project);
    }

    public static void displaySimpleNotification(@NotNull NotificationType type,
                                                 @Nullable Project project,
                                                 @NotNull String title,
                                                 @NotNull String message) {
        Notifications.Bus.notify(new Notification(title, title, replaceNewlines(message), type), project);
    }

    private static String replaceNewlines(String s) {
        return NEWLINE_REGEX.matcher(s).replaceAll("<br/>");
    }

    public static void showHintError(final Editor editor, final String message) {
        HintManager.getInstance().showErrorHint(editor, message);
    }
}
