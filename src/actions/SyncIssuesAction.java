package actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import manager.AndroidIssueManager;
import model.IssuePost;
import tasks.DownloadTask;

import java.util.List;

public class SyncIssuesAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

    }

    private DownloadTask.Listener mDownloadListener = new DownloadTask.Listener() {
        @Override
        public void onDownloadCompleted(List<IssuePost> issues) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    AndroidIssueManager.writePostsToStorage(issues);
                }
            });
        }

        @Override
        public void onDownloadFailed(Exception exception) {
            Notifications.Bus.notify(new Notification("Android Issue Tracker",
                    "Failed", "Could not refresh issues from Google," +
                    " reason: "+ exception.getLocalizedMessage(),  NotificationType.INFORMATION));
        }
    };

}
