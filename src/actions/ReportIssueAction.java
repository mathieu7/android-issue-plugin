package actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Action to report a new issue to Google Issues.
 */
public final class ReportIssueAction extends AnAction {
    /**
     * Android Issue Tracker report URL
     */
    private static final String REPORT_URL = "https://code.google.com/p/android/issues/entry";
    @Override
    public void actionPerformed(final AnActionEvent e) {
        BrowserUtil.open(REPORT_URL);
    }
}
