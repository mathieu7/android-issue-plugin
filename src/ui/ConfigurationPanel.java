package ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import model.ColumnValues;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Provides a configuration panel for project-level configuration.
 */
public class ConfigurationPanel extends JPanel implements DocumentListener {
    private String[] mSelectedColumnSpecs;
    private int mNumberOfRetries;

    private final JTextField retriesTextField = new JTextField();
    private JList totalIssuePropertiesList, selectedIssuePropertiesList;

    private final Project project;
    private boolean isDirty = false;

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
        columnSpecLabel.setText("Issue Descriptors");
        columnSpecLabel.setToolTipText("Enabled columns for issues");
        panel.add(columnSpecLabel);
        totalIssuePropertiesList = createListForProperties(
                ColumnValues.FULL_COLUMN_SPEC);
        panel.add(totalIssuePropertiesList);

        JLabel chosenColumnSpecLabel = new JLabel();
        chosenColumnSpecLabel.setText("Displayed Issue Descriptors");
        chosenColumnSpecLabel.setToolTipText("Selected columns when displaying issues");

        selectedIssuePropertiesList = createListForProperties(mSelectedColumnSpecs);
        JPanel addRemovePanel = new JPanel();
        addRemovePanel.setLayout(new BoxLayout(addRemovePanel, BoxLayout.Y_AXIS));
        JButton addButton = new JButton(">>");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                addSelectedColumns();
            }
        });
        JButton removeButton = new JButton("<<");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                removeSelectedColumns();
            }
        });
        addRemovePanel.add(addButton);
        addRemovePanel.add(removeButton);
        panel.add(addRemovePanel);
        panel.add(chosenColumnSpecLabel);
        panel.add(selectedIssuePropertiesList);

        panel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);
        retriesTextField.getDocument().addDocumentListener(this);
        retriesTextField.setToolTipText(
                "Number of HTTP retries to download issue data (default = 5)");
        JLabel retriesLabel = new JLabel("Download Retries:");
        panel.add(retriesLabel);
        panel.add(retriesTextField);

        return panel;
    }

    public void setSelectedColumnSpecs(final String[] selectedColumnSpecs) {
        mSelectedColumnSpecs = selectedColumnSpecs;
        selectedIssuePropertiesList = createListForProperties(mSelectedColumnSpecs);
    }

    public void setNumberOfRetries(final int numberOfRetries) {
        mNumberOfRetries = numberOfRetries;
        retriesTextField.setText(Integer.toString(numberOfRetries));
    }

    public int getNumberOfRetries() {
        return mNumberOfRetries;
    }

    public boolean hasDirtySelectedProperties() {
        return isDirty;
    }

    private void addSelectedColumns() {
        isDirty = true;
        java.util.List selectedValues = totalIssuePropertiesList.getSelectedValuesList();
        for (Object o : selectedValues) {
            if (!((DefaultListModel) selectedIssuePropertiesList.getModel()).contains(o)) {
                ((DefaultListModel) selectedIssuePropertiesList.getModel()).addElement(o);
            }
        }
    }

    /**
     * Remove the selected columns.
     */
    private void removeSelectedColumns() {
        isDirty = true;
        int[] selectedIndices = selectedIssuePropertiesList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            ((DefaultListModel) selectedIssuePropertiesList.getModel()).removeElementAt(selectedIndices[i]);
        }
    }

    /**
     * Get the currently selected values of issue properties.
     * @return
     */
    public String[] getSelectedIssueProperties() {
        Enumeration enumeration = ((DefaultListModel) selectedIssuePropertiesList.getModel()).elements();
        ArrayList<String> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            Object o = enumeration.nextElement();
            elements.add(String.valueOf(o));
        }
        String[] ret = new String[elements.size()];
        elements.toArray(ret);
        return ret;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        parse();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        parse();
    }

    private void parse() {
        if (Integer.parseInt(retriesTextField.getText()) < 0) {
            retriesTextField.setText("0");
        }
        mNumberOfRetries = Integer.parseInt(retriesTextField.getText());
    }
}