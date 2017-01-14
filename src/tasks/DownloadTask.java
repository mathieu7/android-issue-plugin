package tasks;

import com.intellij.openapi.diagnostic.Logger;
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

/**
 * Task to download issues from Android Issue Tracker
 */
public final class DownloadTask extends Task.Backgroundable {
    /**
     * Logger for debugging purposes.
     */
    private Logger mLogger = Logger.getInstance(DownloadTask.class);

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
            ArrayList<IssuePost> issues =
                    (ArrayList<IssuePost>) AndroidIssueScraper.getInstance().getIssues(progressIndicator);

            // Second, write all the posts to storage
            AndroidIssueManager.writePostsToStorage(issues);

            // Then, For each post, download the issue thread associated by id
            // and write to storage
            for (IssuePost issue: issues) {
                System.out.println("Writing issue thread to storage: "
                        + issue.getId());
                List<IssueComment> issueComments =
                        AndroidIssueScraper.getInstance().getIssueDetail(issue);
                AndroidIssueManager.writeThreadToStorage(
                        issue.getId(), issueComments);
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
