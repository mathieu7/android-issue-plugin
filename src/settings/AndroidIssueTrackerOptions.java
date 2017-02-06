package settings;

public class AndroidIssueTrackerOptions {
    /**
     * Number of retries to fetch issues with.
     */
    private int numberOfRetries;

    /**
     * Default enabled columns to search with.
     */
    private String[] columnSpec = DEFAULT_COLUMN_SPEC;

    /**
     * Default Column Spec used by the plugin.
     */
    public static final String[] DEFAULT_COLUMN_SPEC = {
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
     * All possible column specs to search with.
     */
    public static final String[] FULL_COLUMN_SPEC = {
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
            "Version",
            "Milestone",
            "Attachments",
            "Closed",
            "Modified",
            "BlockedOn",
            "Blocking",
            "Blocked",
            "MergedInto",
            "Cc",
            "Project",
            "Subcomponent",
            "Reportedby",
            "Bash",
            "Triaged",
            "Host"
    };

    public AndroidIssueTrackerOptions() {
        numberOfRetries = 5;
    }

    public String[] getSelectedColumnSpecs() {
        return columnSpec;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }
}