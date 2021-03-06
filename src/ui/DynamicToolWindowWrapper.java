package ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import util.Icons;

import java.util.List;

public final class DynamicToolWindowWrapper {
    private static final String TOOL_WINDOW_TAG = "Find Android Issues";
    private ToolWindow mToolWindow;

    public static DynamicToolWindowWrapper getInstance(final Project project) {
        return ServiceManager.getService(project, DynamicToolWindowWrapper.class);
    }

    public ToolWindow getToolWindow(@NotNull final Project project,
                                    @NotNull final String symbol,
                                    final List<IssuePost> results) {
        if (mToolWindow == null) {
            mToolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_TAG,
                    true, ToolWindowAnchor.BOTTOM);
            mToolWindow.setIcon(Icons.STRUCTURE_TOOL_WINDOW);
        }
        mToolWindow.show(null);

        //set the title of the window to be the token we're looking for.
        mToolWindow.setStripeTitle("Issues Found For: " + symbol);

        IssueResultsView view = new IssueResultsView(project, results, symbol);
        view.setClose(new Runnable() {
            public void run() { mToolWindow.hide(null); }
        });

        Content content = ContentFactory.SERVICE.getInstance().createContent(view, "", false);
        content.setDisposer(view);
        content.setShouldDisposeContent(false);
        ContentManager manager = mToolWindow.getContentManager();
        manager.removeAllContents(true);
        manager.addContent(content);
        manager.setSelectedContent(content);

        return mToolWindow;
    }
}
