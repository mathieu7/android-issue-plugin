package ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import model.ColumnValues;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a configuration panel for project-level configuration.
 */
public class ConfigurationPanel extends JPanel {
    private String[] mSelectedColumnSpecs;
    private int mNumberOfRetries;

    private final JTextField retriesTextField = new JTextField();

    private final Project project;

    public ConfigurationPanel(@NotNull final Project project) {
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
        retriesTextField.setToolTipText(
                "Number of HTTP retries to download issue data (default = 5)");
        panel.add(retriesTextField);

        return panel;
    }

    public void setSelectedColumnSpecs(final String[] selectedColumnSpecs) {
        mSelectedColumnSpecs = selectedColumnSpecs;
    }

    public void setNumberOfRetries(final int numberOfRetries) {
        mNumberOfRetries = numberOfRetries;
        retriesTextField.setText(Integer.toString(numberOfRetries));
    }

    public int getNumberOfRetries() {
        return mNumberOfRetries;
    }
}