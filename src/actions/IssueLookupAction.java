package actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
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

public class IssueLookupAction extends AnAction {
    private Project mProject;
    private String mToken;

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            // Get the caret, the current project, and the current open file.
            Caret caret = e.getData(CommonDataKeys.CARET);
            mProject = e.getProject();
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
            // Find the token under the caret when the user triggered this action.
            int cursorPosition = caret.getOffset();
            PsiElement token = file.findElementAt(cursorPosition);
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
            //TODO: trigger message and quit
            return;
        }

        if (!indexed) {
            //TODO: trigger message and quit
            return;
        }
        try {
            //TODO: get hits from the index, along with issue numbers and threads
            IssueIndex.searchForTerm(mToken);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ArrayList<IssuePost> posts = (ArrayList<IssuePost>) AndroidIssueManager.getIssueListFromStorage();
        showSamplesToolWindow(mProject, posts);
    }

    /**
     * Shows the list of results in a toolwindow panel.
     *
     * @param project The project.
     * @param issues List of SearchResult objects from cloud endpoint generated lib.
     */
    private void showSamplesToolWindow(@NotNull final Project project, final List<IssuePost> issues) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                DynamicToolWindowWrapper toolWindowWrapper = DynamicToolWindowWrapper.getInstance(project);
                ToolWindow toolWindow = toolWindowWrapper.getToolWindow(project, mToken, issues);
                toolWindow.show(null);
            }
        });
    }
}
