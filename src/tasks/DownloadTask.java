package tasks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import manager.AndroidIssueManager;
import model.IssuePost;
import model.IssueThread;
import org.jetbrains.annotations.NotNull;
import scraper.AndroidIssueScraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matt on 12/13/2016.
 */
public class DownloadTask extends Task.Backgroundable {
    /**
     * Logger for debugging purposes
     */
    private Logger mLogger = Logger.getInstance(DownloadTask.class);

    public interface Listener {
        void onDownloadCompleted(final List<IssuePost> issues);
        void onDownloadFailed(final Exception exception);
    }

    /**
     * Progress Indicator strings (TODO: Replace with localized strings)
     */
    private static final String PROGRESS_INDICATOR_TITLE = "Downloading Android Issues...";
    private static final String DOWNLOAD_FAILED_STRING = "Could not download issues from https://code.google.com/android";

    private Listener mListener;

    public DownloadTask(Project project, final Listener listener) {
        super(project, PROGRESS_INDICATOR_TITLE, false);
        setListener(listener);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setText(PROGRESS_INDICATOR_TITLE);
        progressIndicator.setIndeterminate(false);
        try {
            ArrayList<IssuePost> issues = (ArrayList<IssuePost>) AndroidIssueScraper.getInstance().getIssues(progressIndicator);
            if (mListener != null) {
                mListener.onDownloadCompleted(issues);
            }
            /**
             * Iterate over the issue list and download the separate threads.
             * Write those threads to individual files.
             */
            for (IssuePost issue: issues) {
                List<IssueThread> issueThreads = AndroidIssueScraper.getInstance().getIssueDetail(issue);
                AndroidIssueManager.writeThreadsToStorage(issue, issueThreads);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            progressIndicator.setText(DOWNLOAD_FAILED_STRING);
            progressIndicator.cancel();
            if (mListener != null) {
                mListener.onDownloadFailed(ex);
            }
        }
    }
}
