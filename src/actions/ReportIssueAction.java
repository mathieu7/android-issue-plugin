package actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ReportIssueAction extends AnAction {
    private static final String REPORT_URL = "https://code.google.com/p/android/issues/entry";
    @Override
    public void actionPerformed(AnActionEvent e) {
        BrowserUtil.open(REPORT_URL);
    }
}
