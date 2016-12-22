package ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Editor panel that displays the current issue thread, and highlights the relevant search details.
 */
class IssueBrowser extends JPanel implements Disposable {
    private static final Color HIGHLIGHT_BACKGROUND = UIUtil.getTreeUnfocusedSelectionBackground();
    private static final TextAttributes HIGHLIGHT_ATTRIBUTES =
            new TextAttributes(null, HIGHLIGHT_BACKGROUND, null, EffectType.SEARCH_MATCH, Font.BOLD);
    private static final Pattern WHITESPACE_PREFIX = Pattern.compile("^\\s*");
    private final EditorEx editor;
    private final Document document;
    private final Project project;
    private final EditorHighlighterFactory highlighterFactory;


    IssueBrowser(@NotNull Project project) {
        super(new BorderLayout());
        this.project = project;
        highlighterFactory = EditorHighlighterFactory.getInstance();
        EditorFactory factory = EditorFactory.getInstance();
        document = factory.createDocument("");
        editor = (EditorEx) factory.createViewer(document, project);
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setAnimatedScrolling(false);
        settings.setRefrainFromScrolling(true);
        EditorGutterComponentEx gutter = editor.getGutterComponentEx();
        gutter.setShowDefaultGutterPopup(false);
        gutter.revalidateMarkup();
        editor.getSelectionModel().addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChanged(SelectionEvent e) {
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

    void showResult(List<String> issueThreads, final String queryString) {
        if (issueThreads == null || issueThreads.isEmpty()) {
            showEmpty();
            return;
        }

        List<CharSequence> lines = new ArrayList<>();
        lines.addAll(issueThreads);

        List<Integer> highlightLines = new ArrayList<>();
        // LogicalPosition is zero-indexed, but our code is one-indexed.
        LogicalPosition position = new LogicalPosition(0, 0);
        setText(lines, position, highlightLines);
        //String path = ResultUtils.getBaseName(code.getPath());
        //editor.setHighlighter(highlighterFactory.createEditorHighlighter(project, path));

        /*
        CodeResult code = result.getCode();
        List<CharSequence> lines = new ArrayList<CharSequence>();
        int scrollTo = -1;
        List<Integer> highlightLines = new ArrayList<Integer>();
        List<ResultContext> allContext = code.getContext();
        if (allContext != null && !allContext.isEmpty()) {
            // If there's only a single result and it's at line one, it's likely the whole file.
            // Otherwise, the results contain snippets that can be displayed separately.
            boolean containsSnippets = !(allContext.size() == 1 && allContext.get(0).getStartAt() == 1);
            for (ResultContext context : allContext) {
                List<String> src = context.getSrc();
                if (src == null || src.isEmpty()) {
                    continue;
                }
                src = formatCode(src);
                if (!containsSnippets) {
                    if (scrollTo == -1 && !context.getResultsAt().isEmpty()) {
                        scrollTo = context.getResultsAt().get(0);
                    }
                    for (int line : context.getResultsAt()) {
                        highlightLines.add(line - 1);
                    }
                    lines.addAll(formatCode(src));
                    continue;
                }
                // If this is a snippet, then annotate it as such.
                // TODO(thorogood): Different annotations if we ever show other languages (probably just
                // Python).
                StringBuilder dividerBuilder = new StringBuilder();
                int startAt = context.getStartAt();
                int endAt = startAt + src.size() - 1;
                if (endAt == startAt) {
                    dividerBuilder.append("// Line ").append(startAt);
                } else {
                    dividerBuilder.append(String.format("// Lines %d-%d", startAt, endAt));
                    String resultsAt = StringUtil.join(context.getResultsAt(), ", ");
                    if (!StringUtil.isEmpty(resultsAt)) {
                        dividerBuilder.append(" (").append(resultsAt).append(')');
                    }
                }
                lines.add(dividerBuilder);
                for (int line : context.getResultsAt()) {
                    highlightLines.add(lines.size() + line - context.getStartAt());
                }
                lines.addAll(formatCode(src));
                lines.add("");
            }
        }
        if (scrollTo < 1) {
            scrollTo = 1;
        }
        // LogicalPosition is zero-indexed, but our code is one-indexed.
        LogicalPosition position = new LogicalPosition(scrollTo - 1, 0);
        setText(lines, position, highlightLines);
        String path = ResultUtils.getBaseName(code.getPath());
        editor.setHighlighter(highlighterFactory.createEditorHighlighter(project, path));*/
    }

    public JPanel getPanel() {
        return this;
    }

    /**
     * Sets the text and cursor position in the current {@link EditorEx}.
     * @param lines The lines of code to render.
     * @param position The position to center on once lines are set.
     */
    private void setText(@NotNull List<? extends CharSequence> lines,
                         @NotNull final LogicalPosition position,
                         @NotNull final List<Integer> highlight) {
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
                for (int line : highlight) {
                    markup.addLineHighlighter(line, layer, HIGHLIGHT_ATTRIBUTES);
                }
                editor.getGutterComponentEx().revalidateMarkup();
            }
        });
    }
    /**
     * Formats code for display. Currently just trims left whitespace.
     *
     * @param input Source code to format.
     * @return Formatted source code.
     */
    @NotNull
    public static List<String> formatCode(@NotNull List<String> input) {
        boolean knownPrefix = false;
        String prefix = "";
        for (String line : input) {
            Matcher m = WHITESPACE_PREFIX.matcher(line);
            if (!m.find()) {
                throw new IllegalStateException("empty regex should always match");
            }
            if (m.hitEnd()) {
                continue;  // blank line
            }
            String localPrefix = line.substring(0, m.end());
            if (!knownPrefix) {
                prefix = localPrefix;
                knownPrefix = true;
                continue;
            }
            // Find minimum common substring of prefix/localPrefix.
            int j = 0;
            int len = Math.min(prefix.length(), localPrefix.length());
            while (j < len && prefix.charAt(j) == localPrefix.charAt(j)) {
                ++j;
            }
            prefix = prefix.substring(0, j);
        }
        if (prefix.isEmpty()) {
            return input;
        }
        int plen = prefix.length();
        List<String> output = new ArrayList<String>(input.size());
        for (String src : input) {
            if (src.length() < plen) {
                output.add("");
            } else {
                output.add(src.substring(plen));
            }
        }
        return output;
    }
}