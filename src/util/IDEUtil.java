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

/**
 * Miscellaneous utility functions specific to the Intellij IDE.
 */
public final class IDEUtil {

    private IDEUtil() { }

    private static final Pattern NEWLINE_REGEX = Pattern.compile("\n", Pattern.LITERAL);

    public static void displayToolsNotification(@NotNull final NotificationType type,
                                                @NotNull final Project project,
                                                @NotNull final String title,
                                                @NotNull final String message) {
        Notifications.Bus.notify(new Notification(
                title, title,
                replaceNewlines(message) + "<br/><a href='configureAndroidIssuesPlugin'>Configure</a>",
                type, new AndroidIssuesNotificationListener(project)), project);
    }

    public static void displaySimpleNotification(@NotNull final NotificationType type,
                                                 @Nullable final Project project,
                                                 @NotNull final String title,
                                                 @NotNull final String message) {
        Notifications.Bus.notify(new Notification(title, title, replaceNewlines(message), type), project);
    }

    private static String replaceNewlines(final String s) {
        return NEWLINE_REGEX.matcher(s).replaceAll("<br/>");
    }

    public static void showHintError(final Editor editor, final String message) {
        HintManager.getInstance().showErrorHint(editor, message);
    }
}
