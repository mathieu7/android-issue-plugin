import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import com.opencsv.CSVReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matt on 10/21/2016.
 */
public class IssueLookupAction extends AnAction {
    private static final String ISSUES_URL_TEMPLATE = "https://code.google.com/p/android/issues/" +
            "csv?colspec=ID+Status+Priority+Owner+Summary+AllLabels+Stars+Reporter+Opened+OpenedTimestamp&start=%d";

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
                String data = downloadIssuesCSV(100);
                List<String[]> csv = parseCSV(data);
                csv.get(0);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Download the issues from google in a CSV Format.
     * Give an offset for pagination purposes.
     * @param offset
     */
    private String downloadIssuesCSV(int offset) {
        String urlStr = String.format(ISSUES_URL_TEMPLATE, offset);
        URL url;
        StringBuilder output = new StringBuilder();
        try {
            url = new URL(urlStr);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String input;
            while ((input = reader.readLine()) != null) {
                output.append(input);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    /**
     * Helper function for downloading issues with no given offset.
     * @return
     */
    private String downloadIssuesCSV() {
       return downloadIssuesCSV(0);
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
}
