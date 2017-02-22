package tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import manager.AndroidIssueManager;
import model.IssuePost;
import model.IssueComment;
import org.jetbrains.annotations.NotNull;
import scraper.AndroidIssueScraper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Task to download issues from Android Issue Tracker
 */
public final class DownloadTask extends Task.Backgroundable {


    public interface Listener {
        void onDownloadCompleted();
        void onDownloadFailed(Exception exception);
    }

    /**
     * Progress Indicator strings (TODO: Replace with localized strings)
     */
    private static final String PROGRESS_INDICATOR_TITLE = "Downloading Android Issues...";
    private static final String DOWNLOAD_FAILED_STRING = "Could not download issues from https://code.google.com/android";

    private Listener mListener;

    public DownloadTask(final Project project, final Listener listener) {
        super(project, PROGRESS_INDICATOR_TITLE, false);
        setListener(listener);
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setText(PROGRESS_INDICATOR_TITLE);
        progressIndicator.setIndeterminate(false);
        try {
            // First, download all the issue posts
            ArrayList<IssuePost> issues = (ArrayList<IssuePost>)
                    AndroidIssueScraper.getInstance(getProject()).getIssues(progressIndicator);

            // Second, write all the posts to storage
            AndroidIssueManager.writePostsToStorage(issues);

            ExecutorService executorService = Executors.newFixedThreadPool(5);
            // Then, For each post, download the issue thread associated by id
            // and write to storage.
            int count = 0;
            for (IssuePost issue: issues) {
                final int c = count;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(c + " - Writing issue thread to storage: "
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
                count++;
            }

            try {
                executorService.awaitTermination(240L, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
                System.out.println("Timeout reached, executorService suspended");
            }


            // notify observer that we've finished.
            if (mListener != null) {
                mListener.onDownloadCompleted();
            }

        } catch (AndroidIssueScraper.IssueScraperException ex) {
            ex.printStackTrace();
            progressIndicator.setText(DOWNLOAD_FAILED_STRING);
            progressIndicator.cancel();
            if (mListener != null) {
                mListener.onDownloadFailed(ex);
            }
        }
    }
}
