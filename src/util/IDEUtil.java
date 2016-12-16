package util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class IDEUtil {
    /**
     * Open the url in an external browser process.
     * @param url
     */
    public static void openExternalBrowser(@NotNull final String url) {
        System.out.println(String.format("Opening URL: `%s`", url));
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException | RuntimeException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Display a notification.
     * @param groupDisplayId
     * @param title
     * @param content
     * @param type
     */
    public static void showNotification(@NotNull String groupDisplayId, @NotNull String title,
                                        @NotNull String content, @NotNull NotificationType type) {
        Notifications.Bus.notify(new Notification(
                groupDisplayId,
                title,
                content,
                type));
    }
}
