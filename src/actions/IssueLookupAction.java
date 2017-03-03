package actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiJavaFile;
import index.IssueIndex;
import manager.AndroidIssueManager;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import ui.DynamicToolWindowWrapper;
import util.IDEUtil;
import util.PluginTextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to lookup an issue related to Android by class name.
 */
public final class IssueLookupAction extends AnAction {
    private Project mProject;
    private String mToken;
    private int mOffset;

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            // Get the caret, the current project, and the current open file.
            mProject = e.getProject();
            PsiFile file = e.getRequiredData(CommonDataKeys.PSI_FILE);

            final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            CaretModel caretModel = editor.getCaretModel();
            mOffset = caretModel.getOffset();
            // Find the token under the caret when the user triggered this action.
            //int cursorPosition = caret.getOffset();
            PsiElement token = file.findElementAt(mOffset);
            // If it's not null, look up.
            //TODO: Check to see if the token is an actual dependency or object.
            if (file instanceof PsiJavaFile) {
                PsiImportList importList = ((PsiJavaFile) file).getImportList();
                System.out.println(importList.getImportStatements());
            }
            if (token != null) {
                mToken = token.getText();
                executeSearch();
            } else {
                IDEUtil.displaySimpleNotification(
                        NotificationType.ERROR,
                        mProject,
                        PluginTextUtil.getString("plugin_title"),
                        PluginTextUtil.getString("issue_lookup_error_invalid_token"));
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void executeSearch() {
        boolean indexed = IssueIndex.exists();
        boolean cacheExists = AndroidIssueManager.getIssueDirectory().exists();

        if (!cacheExists) {
            IDEUtil.displayToolsNotification(NotificationType.ERROR,
                    mProject,
                    PluginTextUtil.getString("plugin_title"),
                    PluginTextUtil.getString("issue_lookup_error_no_cache"));
            return;
        }

        if (!indexed) {
            IDEUtil.displayToolsNotification(NotificationType.ERROR,
                    mProject,
                    PluginTextUtil.getString("plugin_title"),
                    PluginTextUtil.getString("issue_lookup_error_no_index"));
            return;
        }
        ReadTask readTask = new ReadTask() {

            @Override
            public void computeInReadAction(@NotNull ProgressIndicator indicator) throws ProcessCanceledException {
                ArrayList<String> issueIds = null;
                try {
                    issueIds = IssueIndex.searchForTerm(mToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ArrayList<IssuePost> posts = (ArrayList<IssuePost>)
                        AndroidIssueManager.getFilteredIssueList(issueIds);
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        showSamplesToolWindow(mProject, mToken, posts);
                    }
                });
            }

            @Override
            public void onCanceled(@NotNull ProgressIndicator progressIndicator) {
                System.out.println("ReadTask cancelled");
            }
        };
        ProgressIndicatorUtils.scheduleWithWriteActionPriority(readTask);
    }

    /**
     * Shows the list of results in a toolwindow panel.
     *
     * @param project The project.
     * @param searchToken The token user searched for.
     * @param issues  List of Android Issues to display
     */
    private void showSamplesToolWindow(@NotNull final Project project,
                                       final String searchToken,
                                       final List<IssuePost> issues) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                DynamicToolWindowWrapper toolWindowWrapper =
                        DynamicToolWindowWrapper.getInstance(project);
                ToolWindow toolWindow =
                        toolWindowWrapper.getToolWindow(project,
                                searchToken, issues);
                toolWindow.show(null);
            }
        });
    }
}
