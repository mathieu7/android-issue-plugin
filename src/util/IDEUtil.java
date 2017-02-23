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

    /**
     * Display a tools notification.
     * @param type
     * @param project
     * @param title
     * @param message
     */
    public static void displayToolsNotification(@NotNull final NotificationType type,
                                                @NotNull final Project project,
                                                @NotNull final String title,
                                                @NotNull final String message) {
        Notifications.Bus.notify(new Notification(
                title, title,
                replaceNewlines(message)
                        + "<br/><a href='"
                        + PluginNotificationListener.EVENT_DESCRIPTION
                        + "'>Configure</a>",
                type, new PluginNotificationListener(project)), project);
    }

    /**
     * Display a simple notification.
     * @param type
     * @param project
     * @param title
     * @param message
     */
    public static void displaySimpleNotification(@NotNull final NotificationType type,
                                                 @Nullable final Project project,
                                                 @NotNull final String title,
                                                 @NotNull final String message) {
        Notifications.Bus.notify(new Notification(title, title, replaceNewlines(message), type), project);
    }

    private static String replaceNewlines(final String s) {
        return NEWLINE_REGEX.matcher(s).replaceAll("<br/>");
    }

    /**
     * Display an error hint.
     * @param editor
     * @param message
     */
    public static void showHintError(final Editor editor, final String message) {
        HintManager.getInstance().showErrorHint(editor, message);
    }

    /**
     * Get the string content of the clipboard.
     * @return
     */
    public static String getClipboardContent() {
        try {
            return (String) getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Check if the clipboard is empty.
     * @return
     */
    public static boolean isClipboardEmpty() {
        String content = getClipboardContent();
        return content == null || content.isEmpty();
    }

    /**
     * Copy string to clipboard.
     * @param string
     */
    public static void copyToClipboard(final String string) {
        Clipboard clipboard = getSystemClipboard();
        clipboard.setContents(new StringSelection(string), null);
    }

    /**
     * Get a reference to the system clipboard.
     * @return
     */
    public static Clipboard getSystemClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }
}
