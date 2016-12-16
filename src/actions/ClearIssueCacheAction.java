package actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.util.ui.ConfirmationDialog;
import manager.AndroidIssueManager;
import util.IDEUtil;

public class ClearIssueCacheAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            boolean result = ConfirmationDialog.requestForConfirmation(VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION,
                    e.getProject(),
                    "Confirm clear android issue cache/index?",
                    "Confirm Dialog",
                    Messages.getQuestionIcon());
            if (!result) return;
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    AndroidIssueManager.clearCacheAndIndex();
                }
            });
        } catch (Exception ex) {
            IDEUtil.showNotification("Android Issue Tracker",
                    "Android Issue Tracker Plugin", "Could not clear issue cache and index: " + ex.getLocalizedMessage(),
                    NotificationType.ERROR);
        }
    }
}
