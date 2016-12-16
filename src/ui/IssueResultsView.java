package ui;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.IDEUtil;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * IssueResultsView extends JPanel to show the results of searching for issues.
 */
class IssueResultsView extends JPanel implements Disposable {
    private final Splitter splitter;
    private final IssuePostTable table;
    private final IssueBrowser browser;
    private final JComponent secondComponent;
    private Runnable closeAction;

    IssueResultsView(@NotNull final Project project, final List<IssuePost> results) {
        setLayout(new BorderLayout());
        table = new IssuePostTable(results);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                syncBrowser();
            }
        });

        browser = new IssueBrowser(project);
        splitter = new OnePixelSplitter(false, 0.5f);
        splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(table));
        secondComponent = ScrollPaneFactory.createScrollPane(browser.getPanel());
        splitter.setSecondComponent(secondComponent);
        secondComponent.setVisible(false);  // initially hide browser
        add(splitter, BorderLayout.CENTER);
        createActionsPanel();
        // Add action listeners. These listeners don't do anything if the target node isn't a leaf,
        // because trunk nodes respond by opening/closing on these same events. Users should use the
        // popup menu to open any relevant URLs instead.
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent event) {
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
        ActionManager actionManager = ActionManager.getInstance();
        PopupHandler.installPopupHandler(table, actionGroup, ActionPlaces.USAGE_VIEW_POPUP, actionManager);
    }

    /**
     * @param close The code to run when the Close button is pressed.
     */
    void setClose(Runnable close) {
        this.closeAction = close;
    }

    private void invokeSelectAction(@Nullable IssuePost post) {
        if (post == null) return;
        String url = post.getDetailURL();
        if (url != null) {
            IDEUtil.openExternalBrowser(url);
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
                .createActionToolbar(ActionPlaces.CODE_INSPECTION, group, false).getComponent();
        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.add(actionsToolbar, BorderLayout.WEST);
        add(actionsPanel, BorderLayout.WEST);
    }

    @Override
    public void dispose() {
        splitter.dispose();
        if (browser != null) {
            browser.dispose();
        }
    }

    private IssuePost getSingleSelectedRow() {
        ListSelectionModel selectionModel = table.getSelectionModel();

        if (selectionModel.getLeadSelectionIndex() != -1) {
            return ((IssuePostTable.IssueTableModel) table.getModel()).getRowData(selectionModel.getLeadSelectionIndex());
        }
        return null;
    }

    private void syncBrowser() {
        boolean visible = false;
        IssuePost post = getSingleSelectedRow();
        if (post != null) {
            showInBrowser(post);
            visible = true;
        } else {
            browser.showEmpty();
        }
        if (secondComponent.isVisible() != visible) {
            secondComponent.setVisible(visible);
            splitter.doLayout();
            splitter.doLayout();  // run twice, in case skipNextLayouting() was called on Splitter
        }
    }

    /**
     * Shows the given {@link IssuePost thread} inside the current {@link IssueBrowser}.
     *
     * @param IssuePost
     * @return Whether the result could be shown.
     */
    private boolean showInBrowser(@NotNull IssuePost post) {
        Cursor currentCursor = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        //browser.showResult(post.getDetailURL(), query);
        setCursor(currentCursor);
        return true;
    }

    private class OpenURLAction extends AnAction {
        private OpenURLAction() {
            super(IdeBundle.message("open.url.in.browser.tooltip"), null, null);
        }

        @Override
        public void update(AnActionEvent e) {
            IssuePost post;
            if ((post = getSingleSelectedRow()) != null) {
                String url = post.getDetailURL();
                e.getPresentation().setVisible(url != null);
            }
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            invokeSelectAction(getSingleSelectedRow());
        }
    }

    private class CloseAction extends AnAction implements DumbAware {
        private CloseAction() {
            super(CommonBundle.message("action.close"), null, AllIcons.Actions.Cancel);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            if (closeAction != null) {
                closeAction.run();
            }
        }
    }
}