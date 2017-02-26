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
    private String[] selectedIssueProperties;

    public UserOptions() {
        numberOfRetries = DEFAULT_RETRIES;
        selectedIssueProperties = ColumnValues.DEFAULT_COLUMN_SPEC;
    }

    public String[] getSelectedIssueProperties() {
        return selectedIssueProperties;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(final int retries) {
        numberOfRetries = retries;
    }

    public void setSelectedIssueProperties(final String[] issueProperties) {
        this.selectedIssueProperties = issueProperties;
    }
}