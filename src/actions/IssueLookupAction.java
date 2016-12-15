package actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import index.IssueIndex;
import manager.AndroidIssueManager;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import tasks.DownloadTask;
import tasks.IndexingTask;
import ui.DynamicToolWindowWrapper;

import java.util.ArrayList;
import java.util.List;


public class IssueLookupAction extends AnAction {
    private Project mProject;
    private String mToken;

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            // Get the caret, the current project, and the current open file.
            Caret caret = e.getData(CommonDataKeys.CARET);
            mProject = e.getProject();
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
            // Find the token under the caret when the user triggered this action.
            int cursorPosition = caret.getOffset();
            PsiElement token = file.findElementAt(cursorPosition);
            // If it's not null, look up
            if (token != null) {
                mToken = token.getText();
                executeSearch();
            } else {
                Notifications.Bus.notify(new Notification(
                        "Android Issue Tracker",
                        "Failed",
                        "Invalid token for search",
                        NotificationType.INFORMATION));
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
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
            showSamplesToolWindow(mProject, issues);
        }

        @Override
        public void onDownloadFailed(Exception exception) {
            Notifications.Bus.notify(new Notification("Android Issue Tracker",
                    "Failed", "Could not refresh issues from Google," +
                    " reason: "+ exception.getLocalizedMessage(),  NotificationType.INFORMATION));
        }
    };

    /**
     * Download the latest android issues, scraped from code.google.com/android
     */
    private void downloadIssues() {
        ProgressManager.getInstance().run(new DownloadTask(mProject, mDownloadListener));
    }

    private void executeSearch() {
        boolean indexed = IssueIndex.exists();
        boolean cacheExists = AndroidIssueManager.getIssueDirectory().exists();

        if (!cacheExists) {
            downloadIssues();
            return;
        }

        if (!indexed) {
            ProgressManager.getInstance().run(new IndexingTask(mProject));
        }
        try {
            //TODO: get hits from the index, along with issue numbers and threads
            IssueIndex.searchForTerm(mToken);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        showSamplesToolWindow(mProject, new ArrayList<IssuePost>());
    }

    /**
     * Shows the list of results in a toolwindow panel.
     *
     * @param project The project.
     * @param issues List of SearchResult objects from cloud endpoint generated lib.
     */
    private void showSamplesToolWindow(@NotNull final Project project, final List<IssuePost> issues) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                DynamicToolWindowWrapper toolWindowWrapper = DynamicToolWindowWrapper.getInstance(project);
                ToolWindow toolWindow = toolWindowWrapper.getToolWindow(project, mToken, issues);
                toolWindow.show(null);
            }
        });
    }
}
