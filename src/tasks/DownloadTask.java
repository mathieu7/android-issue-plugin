package tasks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import scraper.AndroidIssues;

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
     * Progress Indicator strings
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
        progressIndicator.setIndeterminate(true);
        try {
            ArrayList<IssuePost> issues = (ArrayList<IssuePost>) AndroidIssues.getInstance().getIssues();
            if (mListener != null) mListener.onDownloadCompleted(issues);
        } catch (IOException ex) {
            ex.printStackTrace();
            progressIndicator.setText(DOWNLOAD_FAILED_STRING);
            progressIndicator.cancel();
            if (mListener != null) mListener.onDownloadFailed(ex);
        }
    }
}
