package ui;

import com.intellij.openapi.project.Project;
import model.ColumnValues;
import model.IssuePost;
import org.jetbrains.annotations.NotNull;
import settings.UserSettings;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.List;


/**
 * JTable subclass to display the Android Issues
 */
class IssuePostTable extends JTable {
    private IssueTableModel mModel;
    /**
     * Constructor.
     * @param results The issues to display
     */
    IssuePostTable(@NotNull Project project, @NotNull final List<IssuePost> results) {
        super(new DefaultTableModel());

        mModel = new IssueTableModel();
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UserSettings settings = UserSettings.getInstance(project);
        String[] columnSpec = settings.getSelectedIssueProperties();
        if (columnSpec == null) {
            columnSpec = ColumnValues.DEFAULT_COLUMN_SPEC;
        }
        mModel.setColumnIdentifiers(columnSpec);
        mModel.setDataVector(results);
        setModel(mModel);
    }

    public IssuePost getRowData(final int rowIndex) {
        return mModel.getRowData(rowIndex);
    }

    private static class IssueTableModel extends AbstractTableModel {
        private List<IssuePost> issuePosts;
        private String[] columnIdentifiers;
        private String[][] postTable;

        public void setColumnIdentifiers(final String[] columnIdentifiers) {
            this.columnIdentifiers = columnIdentifiers;
            fireTableDataChanged();
        }

        public void setDataVector(final List<IssuePost> data) {
            issuePosts = data;
            convertToTableFormat();
            fireTableDataChanged();
        }

        private void convertToTableFormat() {
            if (issuePosts == null) return;
            postTable = new String[issuePosts.size()]
                    [columnIdentifiers.length];
            for (int i = 0; i < issuePosts.size(); i++) {
                postTable[i] = issuePosts.get(i).getAsArray(columnIdentifiers);
            }
        }

        public IssuePost getRowData(final int rowIndex)
        {
            if (rowIndex  > getRowCount() || rowIndex  <  0) {
                return null;
            }
            return issuePosts.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return issuePosts == null || issuePosts.isEmpty() ? 0 : issuePosts.size();
        }

        @Override
        public int getColumnCount() {
            return columnIdentifiers == null ? 0 : columnIdentifiers.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnIdentifiers[column];
        }

        @Override
        public boolean isCellEditable(final int row, final int column) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return postTable[rowIndex][columnIndex];
        }
    }
}
