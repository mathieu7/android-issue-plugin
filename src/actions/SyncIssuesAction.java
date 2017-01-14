package actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import manager.AndroidIssueManager;
import model.IssuePost;
import tasks.DownloadTask;
import tasks.IndexingTask;
import util.IDEUtil;

import java.util.List;

/**
 * Action to sync the current state of Android Issues with the plugin's version.
 */
public final class SyncIssuesAction extends AnAction implements IndexingTask.Listener, DownloadTask.Listener {
    private Project mProject;
    @Override
    public void actionPerformed(final AnActionEvent e) {
        mProject = e.getProject();
        ProgressManager.getInstance().run(new DownloadTask(mProject, this));
    }

    @Override
    public void onDownloadCompleted() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ProgressManager.getInstance().run(
                        new IndexingTask(mProject, SyncIssuesAction.this));
            }
        });
    }

    @Override
    public void onIndexingCompleted() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Messages.showInfoMessage(mProject,
                        "Indexing completed", "Android Issue Tracker Plugin");
            }
        });
    }

    @Override
    public void onIndexingFailed(final String reason) {
        IDEUtil.displaySimpleNotification(NotificationType.ERROR, null,
                "Android Issues Plugin",
                "Could not index issues from Google,"
                        + " reason: " + reason);
    }

    @Override
    public void onDownloadFailed(final Exception exception) {
        IDEUtil.displaySimpleNotification(NotificationType.INFORMATION, null,
                "Android Issues Plugin",
                "Could not refresh issues from Google,"
                        + " reason: " + exception.getLocalizedMessage());
    }
}
