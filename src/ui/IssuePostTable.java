package ui;

import model.IssuePost;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

class IssuePostTable extends JTable {

    private static final String[] sColumnNames = {
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

    IssuePostTable(final List<IssuePost> results) {
        super(new IssueTableModel());

        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ((DefaultTableModel) getModel()).setColumnIdentifiers(sColumnNames);
        // TODO: Create a custom model for the data

        String[][] dataset = new String[results.size()][sColumnNames.length];
        for (int i = 0; i < results.size(); i++) {
            dataset[i] = results.get(i).getAsArray();
        }
        ((DefaultTableModel) getModel()).setDataVector(dataset, sColumnNames);
    }

    private static class IssueTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
