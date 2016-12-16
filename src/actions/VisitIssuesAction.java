package actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class VisitIssuesAction extends AnAction {
    private static final String ISSUE_TRACKER_URL = "https://code.google.com/p/android/issues/list";
    @Override
    public void actionPerformed(AnActionEvent e) {
        BrowserUtil.open(ISSUE_TRACKER_URL);
    }
}
