package ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;

public class DynamicToolWindowWrapper {
    private static final String TOOL_WINDOW_TAG = "Find Android Issues";
    private ToolWindow mToolWindow;

    public static DynamicToolWindowWrapper getInstance(Project project) {
        return ServiceManager.getService(project, DynamicToolWindowWrapper.class);
    }

    public ToolWindow getToolWindow(Project project) {
        if (mToolWindow == null) {
            mToolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_TAG,
                    true, ToolWindowAnchor.BOTTOM);
            mToolWindow.setIcon(AllIcons.Debugger.Watch);
        }
        mToolWindow.show(null);

        //TODO: Add the results view
        /*
        view.setClose(new Runnable() {
        public void run() { mToolWindow.hide(null); }
        });
         */

        /*Content content = ContentFactory.SERVICE.getInstance().createContent(view, "", false);
        content.setDisposer(view);
        content.setShouldDisposeContent(false);
        ContentManager manager = mToolWindow.getContentManager();
        manager.removeAllContents(true);
        manager.addContent(content);
        manager.setSelectedContent(content);*/
        return mToolWindow;
    }
}
