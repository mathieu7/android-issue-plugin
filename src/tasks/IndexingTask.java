package tasks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import index.IssueIndex;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Matt on 12/13/2016.
 */
public class IndexingTask extends Task.Backgroundable {
    /**
     * Logger for debugging purposes
     */
    private Logger mLogger = Logger.getInstance(IndexingTask.class);

    public interface Listener {
        void onIndexingCompleted();
        void onIndexingFailed(String reason);
    }

    /**
     * Progress Indicator title
     */
    private static final String PROGRESS_INDICATOR_TITLE = "Indexing Android Issues...";

    /**
     * Reference to list of Android Issues
     */
    private final List<IssuePost> mIssues;

    private Listener mListener;

    public IndexingTask(Project project) {
        this(project, null);
    }

    public IndexingTask(Project project, final List<IssuePost> issues) {
        super(project, PROGRESS_INDICATOR_TITLE, false);
        mIssues = issues;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true);
        if (mIssues == null) {
            // if we haven't passed issues, just index the current directory if possible.
            mLogger.debug("No passed issues, indexing current issue directory...");
            try {
                IssueIndex.indexIssueDirectory();
                if (mListener != null) mListener.onIndexingCompleted();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                if (mListener != null) mListener.onIndexingFailed(ex.getMessage());
            }
            return;
        }

        //TODO: Otherwise, let's write the issues to a file and index.

    }
}
