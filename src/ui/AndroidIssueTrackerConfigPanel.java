package ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import model.ColumnValues;
import org.jetbrains.annotations.NotNull;
import settings.AndroidIssueTrackerOptions;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a configuration panel for project-level configuration.
 */
public class AndroidIssueTrackerConfigPanel extends JPanel {
    private static final Insets COMPONENT_INSETS = JBUI.insets(4);
    private static final int ACTIVE_COL_MIN_WIDTH = 40;
    private static final int ACTIVE_COL_MAX_WIDTH = 50;
    private static final int DESC_COL_MIN_WIDTH = 100;
    private static final int DESC_COL_MAX_WIDTH = 200;
    private static final Dimension DECORATOR_DIMENSIONS = new Dimension(300, 50);

    private String[] mSelectedColumnSpecs;
    private int mNumberOfRetries;

    private final JCheckBox suppressErrorsCheckbox = new JCheckBox();

    private final Project project;

    public AndroidIssueTrackerConfigPanel(@NotNull final Project project) {
        super(new BorderLayout());
        this.project = project;
    }

    /**
     * Generate a list with multiple choices for options.
     * @param properties
     * @return
     */
    private JList createListForProperties(final Object[] properties) {
        JList list = new JBList(properties);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        return list;
    }

    public void initializeConfigPanel() {
        add(buildConfigPanel(), BorderLayout.CENTER);
    }

    private JPanel buildConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JLabel columnSpecLabel = new JLabel();
        columnSpecLabel.setText("Possible Columns");
        columnSpecLabel.setToolTipText("Enable columns for searching issues");
        panel.add(columnSpecLabel);
        JList availablePropertiesList = createListForProperties(
                ColumnValues.FULL_COLUMN_SPEC);
        panel.add(availablePropertiesList);

        JLabel chosenColumnSpecLabel = new JLabel();
        chosenColumnSpecLabel.setText("Selected Columns");
        chosenColumnSpecLabel.setToolTipText("Selected columns when searching issues");

        JList selectedPropertiesList = createListForProperties(mSelectedColumnSpecs);
        JPanel addRemovePanel = new JPanel();
        addRemovePanel.setLayout(new BoxLayout(addRemovePanel, BoxLayout.Y_AXIS));
        JButton addButton = new JButton(">>");
        JButton removeButton = new JButton("<<");
        addRemovePanel.add(addButton);
        addRemovePanel.add(removeButton);
        panel.add(addRemovePanel);
        panel.add(chosenColumnSpecLabel);
        panel.add(selectedPropertiesList);

        panel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);
        //panel.add(suppressErrorsCheckbox);

        return panel;
    }


    public void setSuppressingErrors(final boolean suppressingErrors) {
        suppressErrorsCheckbox.setSelected(suppressingErrors);
    }

    public boolean isSuppressingErrors() {
        return suppressErrorsCheckbox.isSelected();
    }

    public void setSelectedColumnSpecs(final String[] selectedColumnSpecs) {
        mSelectedColumnSpecs = selectedColumnSpecs;
    }

    public void setNumberOfRetries(final int numberOfRetries) {
        mNumberOfRetries = numberOfRetries;
    }
}