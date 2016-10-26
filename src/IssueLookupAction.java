import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import com.opencsv.CSVReader;
import model.IssueThread;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class IssueLookupAction extends AnAction {
    private Project mProject;
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            // Get the caret, the current project, and the current open file.
            Caret caret = e.getData(CommonDataKeys.CARET);
            mProject = e.getProject();
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
            // Find the token under the caret when the user triggered this action.
            PsiElement token = file.findElementAt(caret.getOffset());

            // If it's not null, look up
            if (token != null) {
                Messages.showMessageDialog(mProject, "Looking for AOSP issues for : " + token.getText(),
                        "Dialog", Messages.getInformationIcon());
                executeDownload();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private List<String[]> parseCSV(String data) {
        List<String[]> entries = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new StringReader(data));
            entries = reader.readAll();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    /**
     * Download the latest list of android issues
     */
    private void executeDownload() {
        ProgressManager.getInstance().run(new Task.Backgroundable(mProject, "Downloading Android Issues...") {
            public void run(@NotNull ProgressIndicator progressIndicator) {

                progressIndicator.setText("Downloading android issues...");
                progressIndicator.setIndeterminate(true);

                try {
                    ArrayList<IssueThread> threads = (ArrayList<IssueThread>) AndroidIssues.getInstance().getIssueDetail(97663);
                    System.out.println(threads.get(0).getComment());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    progressIndicator.setText("Could not download issues from https://code.google.com/android");
                    progressIndicator.cancel();
                }
            }
        });
    }
}
