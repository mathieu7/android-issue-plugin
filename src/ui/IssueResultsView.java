package ui;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import manager.AndroidIssueManager;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.IDEUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * IssueResultsView extends JPanel to show the results of searching for issues.
 */
class IssueResultsView extends JPanel implements Disposable {
    private final Splitter splitter;
    private final IssuePostTable table;
    private final IssueThreadListView threadListView;
    private final JComponent secondComponent;
    private Runnable closeAction;
    private String queryString;

    IssueResultsView(@NotNull final Project project,
                     final List<IssuePost> results,
                     final String queryString) {
        setLayout(new BorderLayout());
        table = new IssuePostTable(project, results);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                displaySelectedIssueThread();
            }
        });
        //TODO: Need better sorting.
        table.setAutoCreateRowSorter(true);
        this.queryString = queryString;
        threadListView = new IssueThreadListView(project);
        splitter = new OnePixelSplitter(false, 0.5f);
        splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(table));
        secondComponent = ScrollPaneFactory.createScrollPane(threadListView.getPanel());
        splitter.setSecondComponent(secondComponent);
        secondComponent.setVisible(false);  // initially hide browser
        add(splitter, BorderLayout.CENTER);
        createActionsPanel();
        // Add action listeners.
        // These listeners don't do anything if the target node isn't a leaf,
        // because trunk nodes respond by opening/closing on these same events.
        // Users should use the popup menu to open any relevant URLs instead.
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(final MouseEvent event) {
                IssuePost post = getSingleSelectedRow();
                invokeSelectAction(post);
                return true;
            }
        }.installOn(table);
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                IssuePost post = getSingleSelectedRow();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    invokeSelectAction(post);
                }
            }
        });
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new OpenURLAction());
        actionGroup.add(new CopyIssueToClipboardAction());
        ActionManager actionManager = ActionManager.getInstance();
        PopupHandler.installPopupHandler(table,
                actionGroup, ActionPlaces.USAGE_VIEW_POPUP, actionManager);
    }

    /**
     * @param close The code to run when the Close button is pressed.
     */
    void setClose(final Runnable close) {
        this.closeAction = close;
    }

    private void invokeSelectAction(@Nullable final IssuePost post) {
        if (post == null) return;
        String url = post.getDetailURL();
        if (url != null) {
            BrowserUtil.open(url);
        }
    }

    /**
     * Create and add a JPanel containing default actions. This just includes Close for now.
     */
    private void createActionsPanel() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new CloseAction());
        ActionManager actionManager = ActionManager.getInstance();
        JComponent actionsToolbar = actionManager
                .createActionToolbar(ActionPlaces.CODE_INSPECTION, group, false)
                .getComponent();
        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.add(actionsToolbar, BorderLayout.WEST);
        add(actionsPanel, BorderLayout.WEST);
    }

    @Override
    public void dispose() {
        splitter.dispose();
        if (threadListView != null) {
            threadListView.dispose();
        }
    }

    private static final int NOT_SELECTED = -1;

    /**
     * Get the selected issue row.
     * @return
     */
    private IssuePost getSingleSelectedRow() {
        ListSelectionModel selectionModel = table.getSelectionModel();
        int selectedIndex = selectionModel.getLeadSelectionIndex();
        if (selectedIndex != NOT_SELECTED) {
            return table.getRowData(selectedIndex);
        }
        return null;
    }

    /**
     * Fill the browser with the currently selected issue row thread.
     */
    private void displaySelectedIssueThread() {
        boolean visible = false;
        IssuePost post = getSingleSelectedRow();
        if (post != null) {
            showIssueThread(post);
            visible = true;
        } else {
            threadListView.showEmpty();
        }
        if (secondComponent.isVisible() != visible) {
            secondComponent.setVisible(visible);
            splitter.doLayout();
            splitter.doLayout();
            // run twice, in case skipNextLayouting() was called on Splitter
        }
    }

    /**
     * Shows the given
     * {@link IssuePost thread} inside the current {@link IssueThreadListView}.
     *
     * @param post
     * @param queryString
     * @return Whether the result could be shown.
     */
    private boolean showIssueThread(@NotNull final IssuePost post) {
        List<String> issueThreads =
                AndroidIssueManager.getIssueThreadLinesFromId(post.getId());
        threadListView.showThread(issueThreads, queryString);
        return true;
    }

    private class OpenURLAction extends AnAction {
        private OpenURLAction() {
            super(IdeBundle.message("open.url.in.browser.tooltip"), null, null);
        }

        @Override
        public void update(final AnActionEvent e) {
            IssuePost post;
            if ((post = getSingleSelectedRow()) != null) {
                String url = post.getDetailURL();
                e.getPresentation().setVisible(url != null);
            }
        }

        @Override
        public void actionPerformed(final AnActionEvent e) {
            invokeSelectAction(getSingleSelectedRow());
        }
    }

    private class CloseAction extends AnAction implements DumbAware {
        private CloseAction() {
            super(CommonBundle.message("action.close"), null, AllIcons.Actions.Cancel);
        }

        @Override
        public void actionPerformed(final AnActionEvent e) {
            if (closeAction != null) {
                closeAction.run();
            }
        }
    }

    private class CopyIssueToClipboardAction extends AnAction {

        private CopyIssueToClipboardAction() {
            super("Copy issue URL to clipboard", null, null);
        }

        @Override
        public void actionPerformed(final AnActionEvent e) {
            IssuePost post = getSingleSelectedRow();
            if (post == null) return;
            String url = post.getDetailURL();
            final String comment = "// " + url;
            IDEUtil.copyToClipboard(comment);
        }
    }
}
