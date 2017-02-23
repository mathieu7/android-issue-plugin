package settings;

import model.ColumnValues;

public class UserOptions {
    private static final int DEFAULT_RETRIES = 5;
    /**
     * Number of retries to fetch issues with.
     */
    private int numberOfRetries;

    /**
     * Default enabled columns to search with.
     */
    private String[] columnSpec = ColumnValues.DEFAULT_COLUMN_SPEC;

    public UserOptions() {
        numberOfRetries = DEFAULT_RETRIES;
    }

    public String[] getSelectedColumnSpecs() {
        return columnSpec;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }
}