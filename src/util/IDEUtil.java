package util;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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

    public static String getClipboardContent() {
        try {
            return (String) getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception ignored) {
        }

        return null;
    }

    public static boolean isClipboardEmpty() {
        String content = getClipboardContent();
        return content == null || content.isEmpty();
    }

    /**
     * @param   str
     */
    public static void copyToClipboard(final String str) {
        Clipboard clipboard = getSystemClipboard();
        clipboard.setContents(new StringSelection(str), null);
    }

    public static Clipboard getSystemClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }
}
