package tasks;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import manager.AndroidIssueManager;
import org.jetbrains.annotations.NotNull;

/**
 * Task to delete all downloaded data and indices.
 */
public final class DeleteDataTask extends Task.Backgroundable {
    /**
     * Logger for debugging purposes
     */
    private Logger mLogger = Logger.getInstance(DeleteDataTask.class);
    /**
     * Progress Indicator title
     */
    private static final String PROGRESS_INDICATOR_TITLE = "Deleting Android Issues/Indices...";

    public DeleteDataTask(final Project project) {
        super(project, PROGRESS_INDICATOR_TITLE, false);
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true);
        mLogger.debug(PROGRESS_INDICATOR_TITLE);
        AndroidIssueManager.clearCacheAndIndex();
    }
}
