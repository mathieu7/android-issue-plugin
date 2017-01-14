package actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public final class VisitIssuesAction extends AnAction {
    /**
     * Google's Issue Tracker URL
     */
    private static final String ISSUE_TRACKER_URL = "https://code.google.com/p/android/issues/list";

    @Override
    public void actionPerformed(final AnActionEvent e) {
        BrowserUtil.open(ISSUE_TRACKER_URL);
    }
}
