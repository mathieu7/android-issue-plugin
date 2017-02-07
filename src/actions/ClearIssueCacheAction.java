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
import util.PluginTextUtil;

/**
 * Simple action to clear the issue caches/indices upon confirmation.
 */
public final class ClearIssueCacheAction extends AnAction {

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            boolean result = ConfirmationDialog.requestForConfirmation(
                    VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION,
                    e.getProject(),
                    PluginTextUtil.getString("clear_issue_cache_confirm_msg"),
                    PluginTextUtil.getString("clear_issue_cache_confirm_title"),
                    Messages.getQuestionIcon());
            if (!result) return;
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    AndroidIssueManager.clearCacheAndIndex();
                }
            });
        } catch (Exception ex) {
            IDEUtil.displaySimpleNotification(NotificationType.ERROR,
                    e.getProject(),
                    PluginTextUtil.getString("plugin_title"),
                    "Could not clear issue cache and index: "
                            + ex.getLocalizedMessage()
            );
        }
    }
}
