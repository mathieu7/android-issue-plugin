package ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Editor panel that displays the current issue thread, and highlights the relevant search details.
 */
class IssueThreadListView extends JPanel implements Disposable {
    /**
     * Highlight Background color
     */
    private static final Color HIGHLIGHT_BACKGROUND = UIUtil.getTreeUnfocusedSelectionBackground();
    /**
     * Attributes for Highlighting
     */
    private static final TextAttributes HIGHLIGHT_ATTRIBUTES =
            new TextAttributes(null, HIGHLIGHT_BACKGROUND, null, EffectType.SEARCH_MATCH, Font.BOLD);

    /**
     * Editor
     */
    private final EditorEx editor;

    /**
     * Document we're highlighting
     */
    private final Document document;

    private final Project project;


    IssueThreadListView(@NotNull final Project project) {
        super(new BorderLayout());
        this.project = project;
        EditorFactory factory = EditorFactory.getInstance();
        document = factory.createDocument("");
        editor = (EditorEx) factory.createViewer(document, project);
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(false);
        settings.setAnimatedScrolling(false);
        settings.setRefrainFromScrolling(true);
        EditorGutterComponentEx gutter = editor.getGutterComponentEx();
        gutter.setShowDefaultGutterPopup(false);
        gutter.revalidateMarkup();
        editor.getSelectionModel().addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChanged(final SelectionEvent e) {
                TextRange range = e.getNewRange();
                if (range.isEmpty()) {
                    return;
                }
            }
        });
        // Add the component. Configure the preferred size to zero, as otherwise the Editor itself will
        // grow rather than fitting inside its scroll pane.
        JComponent component = editor.getComponent();
        component.setPreferredSize(new Dimension(0, 0));
        add(component, BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        EditorFactory factory = EditorFactory.getInstance();
        factory.releaseEditor(editor);
    }

    void showEmpty() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                document.setText("");
            }
        });
    }

    void showThread(final List<String> issueThreads, final String queryString) {
        if (issueThreads == null || issueThreads.isEmpty()) {
            showEmpty();
            return;
        }

        List<CharSequence> lines = new ArrayList<>();
        lines.addAll(issueThreads);

        List<Integer> highlightLines = new ArrayList<>();
        int i = 0;
        for (String thread : issueThreads) {
            if (thread.contains(queryString)) {
                highlightLines.add(i);
            }
            i++;
        }
        LogicalPosition position = new LogicalPosition(0, 0);
        setText(lines, position, highlightLines);
    }

    public JPanel getPanel() {
        return this;
    }

    /**
     * Sets the text and cursor position in the current {@link EditorEx}.
     * @param lines The lines of text to render.
     * @param position The position to center on once lines are set.
     * @param highlightedLines The lines of text that should be highlighted.
     */
    private void setText(@NotNull final List<? extends CharSequence> lines,
                         @NotNull final LogicalPosition position,
                         @NotNull final List<Integer> highlightedLines) {
        final MarkupModel markup = editor.getMarkupModel();
        markup.removeAllHighlighters();
        final StringBuilder text = new StringBuilder();
        for (CharSequence line : lines) {
            text.append(line).append('\n');
        }
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                document.setText(text);
                editor.getCaretModel().moveToLogicalPosition(position);
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                int layer = HighlighterLayer.SELECTION - 1;
                for (int line : highlightedLines) {
                    markup.addLineHighlighter(line, layer, HIGHLIGHT_ATTRIBUTES);
                }
                editor.getGutterComponentEx().revalidateMarkup();
            }
        });
    }
}
