package util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import settings.AndroidIssueTrackerConfigurable;

import javax.swing.event.HyperlinkEvent;

public final class AndroidIssuesNotificationListener implements NotificationListener {
    private static final String EVENT_DESCRIPTION = "configureAndroidIssuesPlugin";

    private final Project myProject;

    public AndroidIssuesNotificationListener(@NotNull final Project project) {
        myProject = project;
    }

    /**
     * Shows the settings dialog when the user presses "configure" on a balloon.
     */
    @Override
    public void hyperlinkUpdate(@NotNull final Notification notification,
                                @NotNull final HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (event.getDescription().equals(EVENT_DESCRIPTION)
                    && !myProject.isDisposed()) {
                ShowSettingsUtil.getInstance().showSettingsDialog(myProject, AndroidIssueTrackerConfigurable.ID);
                notification.expire();
            }
        }
    }
}
