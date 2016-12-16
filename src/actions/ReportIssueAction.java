package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import util.IDEUtil;

public class ReportIssueAction extends AnAction {
    private static final String REPORT_URL = "https://code.google.com/p/android/issues/entry";
    @Override
    public void actionPerformed(AnActionEvent e) {
        IDEUtil.openExternalBrowser(REPORT_URL);
    }
}
