package model;

/**
 * Class representing an android issue listing.
 */
public class IssuePost {
    public enum Column {
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
    }

    private String mId;
    private String mStatus;
    private String mPriority;
    private String mOwner;
    private String mSummary;
    private String mStars;
    private String mReporter;
    private String mOpened;
    private String mComponent;
    private String mType;
    private String mVersion;

    public static class Builder {
        private IssuePost mInstance;

        public Builder() {
            mInstance = new IssuePost();
        }

        public Builder addValue(Column column, String text) {
            switch (column) {
                case ID:
                    mInstance.mId = text;
                    break;
                case STATUS:
                    mInstance.mStatus = text;
                    break;
                case PRIORITY:
                    mInstance.mPriority = text;
                    break;
                case OWNER:
                    mInstance.mOwner = text;
                    break;
                case SUMMARY:
                    mInstance.mSummary = text;
                    break;
                case STARS:
                    mInstance.mStars = text;
                    break;
                case REPORTER:
                    mInstance.mReporter = text;
                    break;
                case OPENED:
                    mInstance.mOpened = text;
                    break;
                case TYPE:
                    mInstance.mType = text;
                    break;
                case COMPONENT:
                    mInstance.mComponent = text;
                    break;
                case VERSION:
                    mInstance.mVersion = text;
                    break;
            }
            return this;
        }

        public IssuePost build() {
            return mInstance;
        }
    }
}
