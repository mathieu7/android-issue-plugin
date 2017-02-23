package settings;

import model.ColumnValues;

public class UserOptions {
    /**
     * Number of retries to fetch issues with.
     */
    private int numberOfRetries;

    /**
     * Default enabled columns to search with.
     */
    private String[] columnSpec = ColumnValues.DEFAULT_COLUMN_SPEC;

    public UserOptions() {
        numberOfRetries = 5;
    }

    public String[] getSelectedColumnSpecs() {
        return columnSpec;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }
}