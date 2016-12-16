package util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.jetbrains.annotations.NotNull;

public class IDEUtil {
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
