package actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import manager.AndroidIssueManager;
import model.IssuePost;
import tasks.DownloadTask;
import util.IDEUtil;

import java.util.List;

public class SyncIssuesAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ProgressManager.getInstance().run(new DownloadTask(e.getProject(), mDownloadListener));
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
            IDEUtil.displaySimpleNotification( NotificationType.INFORMATION, null,
                    "Android Issues Plugin",
                    "Could not refresh issues from Google," +
                    " reason: "+ exception.getLocalizedMessage());
        }
    };

}
