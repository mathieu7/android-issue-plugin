package tasks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import index.IssueIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Task to index the downloaded issues.
 */
public final class IndexingTask extends Task.Backgroundable {
    /**
     * Logger for debugging purposes
     */
    private Logger mLogger = Logger.getInstance(IndexingTask.class);

    public interface Listener {
        /**
         * Observer function to be ran when indexing task is completed.
         */
        void onIndexingCompleted();

        /**
         * Observer function to be ran when indexing fails (given a reason message)
         * @param reason
         */
        void onIndexingFailed(String reason);
    }

    /**
     * Progress Indicator title
     */
    private static final String PROGRESS_INDICATOR_TITLE = "Indexing Android Issues...";

    private Listener mListener;

    public IndexingTask(final Project project) {
        this(project, null);
    }

    public IndexingTask(final Project project, final Listener listener) {
        super(project, PROGRESS_INDICATOR_TITLE, false);
        setListener(listener);
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true);
        mLogger.debug("Indexing current issue directory...");
        try {
            IssueIndex.indexIssueDirectory();
            if (mListener != null) {
                mListener.onIndexingCompleted();
            }
        } catch (IllegalAccessException | IOException ex) {
            ex.printStackTrace();
            if (mListener != null) {
                mListener.onIndexingFailed(ex.getMessage());
            }
        }
    }
}
