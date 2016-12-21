package util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

public class AndroidIssuesNotificationListener implements NotificationListener {
    private static final String EVENT_DESCRIPTION = "configureAndroidIssuesPlugin";
    @NotNull
    private final Project myProject;

    public AndroidIssuesNotificationListener(@NotNull Project project) {
        myProject = project;
    }

    /**
     * Shows the settings dialog when the user presses "configure" on a balloon.
     */
    @Override
    public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (event.getDescription().equals(EVENT_DESCRIPTION) && !myProject.isDisposed()) {
                //ShowSettingsUtil.getInstance().showSettingsDialog(myProject, HaskellToolsConfigurable.HASKELL_TOOLS_ID);
                notification.expire();
            }
        }
    }
}
