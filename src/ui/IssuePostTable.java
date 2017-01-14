package ui;

import model.IssuePost;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * JTable subclass to display the Android Issues
 */
class IssuePostTable extends JTable {

    /**
     * Column names for Issue Post Table
     */
    private static final String[] COLUMN_NAMES = {
            "Id",
            "Status",
            "Priority",
            "Owner",
            "Summary",
            "Stars",
            "Reporter",
            "Opened",
            "Component",
            "Type",
            "Version"
    };

    /**
     * Constructor.
     * @param results The issues to display
     */
    IssuePostTable(@NotNull final List<IssuePost> results) {
        super(new IssueTableModel());

        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ((DefaultTableModel) getModel()).setColumnIdentifiers(COLUMN_NAMES);
        // TODO: Create a custom model for the data

        String[][] dataset = new String[results.size()][COLUMN_NAMES.length];
        for (int i = 0; i < results.size(); i++) {
            dataset[i] = results.get(i).getAsArray();
        }
        ((DefaultTableModel) getModel()).setDataVector(dataset, COLUMN_NAMES);
    }

    static class IssueTableModel extends DefaultTableModel {
        IssuePost getRowData(final int rowIndex)
        {
            if (rowIndex  > getRowCount() || rowIndex  <  0) {
                return null;
            }
            IssuePost.Builder builder = new IssuePost.Builder();
            final int columnCount = getColumnCount();
            for (int c = 0; c  <  columnCount; c++) {
                IssuePost.Column column = IssuePost.Column.values()[c];
                builder.addValue(column, (String) getValueAt(rowIndex, c));
            }
            return builder.build();
        }

        @Override
        public boolean isCellEditable(final int row, final int column) {
            return false;
        }
    }
}
