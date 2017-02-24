package tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import manager.AndroidIssueManager;
import model.IssueComment;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import scraper.AndroidIssueScraper;
import util.PluginTextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Task to download issues from Android Issue Tracker
 */
public final class DownloadTask extends Task.Backgroundable {
    private static final long EXECUTOR_TIMEOUT = 240L;

    public interface Listener {
        void onDownloadCompleted();
        void onDownloadFailed(Exception exception);
    }

    private Listener mListener;

    public DownloadTask(final Project project, final Listener listener) {
        super(project, PluginTextUtil.getString("download_task_indicator_title"), false);
        setListener(listener);
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setText(PluginTextUtil.getString("download_task_indicator_title"));
        progressIndicator.setIndeterminate(false);
        ArrayList<IssuePost> issues = new ArrayList<>();
        try {
            // First, download all the issue posts
            issues = (ArrayList<IssuePost>)
                    AndroidIssueScraper.getInstance(getProject()).getIssues(progressIndicator);

            // Second, write all the posts to storage
            AndroidIssueManager.writePostsToStorage(issues);
        } catch (AndroidIssueScraper.IssueScraperException ex) {
            ex.printStackTrace();
            progressIndicator.setText(PluginTextUtil.getString("download_task_failed"));
            progressIndicator.cancel();
            if (mListener != null) {
                mListener.onDownloadFailed(ex);
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // Then, For each post, download the issue thread associated by id
        // and write to storage.
        for (IssuePost issue : issues) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Writing issue thread to storage: "
                            + issue.getId());
                    try {
                        List<IssueComment> issueComments =
                                AndroidIssueScraper.getInstance(getProject()).getIssueDetail(issue);
                        AndroidIssueManager.writeThreadToStorage(
                                issue.getId(), issueComments);
                    } catch (AndroidIssueScraper.IssueScraperException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(EXECUTOR_TIMEOUT,
                    TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
            System.out.println("Timeout reached, executorService suspended");
        }
        // notify observer that we've finished.
        if (mListener != null) {
            mListener.onDownloadCompleted();
        }
    }
}
