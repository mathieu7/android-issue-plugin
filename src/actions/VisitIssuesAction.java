package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import util.IDEUtil;

public class VisitIssuesAction extends AnAction {
    private static final String ISSUE_TRACKER_URL = "https://code.google.com/p/android/issues/list";
    @Override
    public void actionPerformed(AnActionEvent e) {
        IDEUtil.openExternalBrowser(ISSUE_TRACKER_URL);
    }
}
