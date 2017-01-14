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
        void onDownloadCompleted(List<IssuePost> issues);
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
            ArrayList<IssuePost> issues =
                    (ArrayList<IssuePost>) AndroidIssueScraper.getInstance().getIssues(progressIndicator);
            if (mListener != null) {
                mListener.onDownloadCompleted(issues);
            }
            /**
             * Iterate over the issue list and download the separate threads.
             * Write those threads to individual files.
             */
            for (IssuePost issue: issues) {
                List<IssueComment> issueComments = AndroidIssueScraper.getInstance().getIssueDetail(issue);
                AndroidIssueManager.writeThreadsToStorage(issue, issueComments);
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
