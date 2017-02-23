package model;

public class ColumnValues {
    public static final String ID = "Id";
    public static final String STATUS = "Status";
    public static final String PRIORITY = "Priority";
    public static final String OWNER = "Owner";
    public static final String SUMMARY = "Summary";
    public static final String STARS = "Stars";
    public static final String REPORTER = "Reporter";
    public static final String OPENED = "Opened";
    public static final String COMPONENT = "Component";
    public static final String TYPE = "Type";
    public static final String VERSION = "Version";
    public static final String MILESTONE = "Milestone";
    public static final String ATTACHMENTS = "Attachments";
    public static final String CLOSED = "Closed";
    public static final String MODIFIED = "Modified";
    public static final String BLOCKEDON = "BlockedOn";
    public static final String BLOCKING = "Blocking";
    public static final String BLOCKED = "Blocked";
    public static final String MERGEDINTO = "MergedInto";
    public static final String CC = "Cc";
    public static final String PROJECT = "Project";
    public static final String SUBCOMPONENT = "Subcomponent";
    public static final String REPORTEDBY = "Reportedby";
    public static final String BASH = "Bash";
    public static final String TRIAGED = "Triaged";
    public static final String HOST = "Host";

    /**
     * Default Column Spec used by the plugin.
     */
    public static final String[] DEFAULT_COLUMN_SPEC = {
            ID,
            STATUS,
            PRIORITY,
            OWNER,
            SUMMARY,
            STARS,
            REPORTER,
            OPENED,
            COMPONENT,
            TYPE,
            VERSION
    };

    /**
     * All possible column specs to search with.
     */
    public static final String[] FULL_COLUMN_SPEC = {
            ID,
            STATUS,
            PRIORITY,
            OWNER,
            SUMMARY,
            STARS,
            REPORTER,
            OPENED,
            COMPONENT,
            TYPE,
            VERSION,
            MILESTONE,
            ATTACHMENTS,
            CLOSED,
            MODIFIED,
            BLOCKEDON,
            BLOCKING,
            BLOCKED,
            MERGEDINTO,
            CC,
            PROJECT,
            SUBCOMPONENT,
            REPORTEDBY,
            BASH,
            TRIAGED,
            HOST
    };
}
