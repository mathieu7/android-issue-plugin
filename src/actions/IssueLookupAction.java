package actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Action to lookup an issue related to Android by class name.
 */
public final class IssueLookupAction extends AnAction {
    private Project mProject;
    private String mToken;

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            // Get the caret, the current project, and the current open file.
            Caret caret = e.getData(CommonDataKeys.CARET);
            mProject = e.getProject();
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);

            final Editor editor = e.getData(CommonDataKeys.EDITOR);
            CaretModel caretModel = editor.getCaretModel();
            int offset = caretModel.getOffset();
            // Find the token under the caret when the user triggered this action.
            //int cursorPosition = caret.getOffset();
            PsiElement token = file.findElementAt(offset);
            // If it's not null, look up
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
                        "Android Issue Tracker Plugin",
                        "Failed: Invalid token/file type for search");
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
                    "Android Issue Tracker Plugin",
                    "No stored issues");
            return;
        }

        if (!indexed) {
            IDEUtil.displayToolsNotification(NotificationType.ERROR,
                    mProject,
                    "Android Issue Tracker Plugin",
                    "No indexed issues");
            return;
        }
        ArrayList<String> issueIds = null;
        try {
            issueIds = IssueIndex.searchForTerm(mToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<IssuePost> posts = (ArrayList<IssuePost>)
                AndroidIssueManager.getFilteredIssueList(issueIds);
        showSamplesToolWindow(mProject, posts);
    }

    /**
     * Shows the list of results in a toolwindow panel.
     *
     * @param project The project.
     * @param issues  List of Android Issues to display
     */
    private void showSamplesToolWindow(@NotNull final Project project,
                                       final List<IssuePost> issues) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                DynamicToolWindowWrapper toolWindowWrapper =
                        DynamicToolWindowWrapper.getInstance(project);
                ToolWindow toolWindow =
                        toolWindowWrapper.getToolWindow(project, mToken, issues);
                toolWindow.show(null);
            }
        });
    }
}
