package model;

import java.io.Serializable;

/**
 * Class representing an android issue listing.
 */
public class IssuePost implements Serializable {
    private static final String DETAIL_URL_TEMPLATE =
            "https://code.google.com/p/android/issues/detail?id=%s";
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

    public String[] getAsArray() {
        return new String[] {
                mId,
                mStatus,
                mPriority,
                mOwner,
                mSummary,
                mStars,
                mReporter,
                mOpened,
                mComponent,
                mType,
                mVersion
        };
    }

    public final String getId() { return mId; }
    public final String getDetailURL() {
        return String.format(DETAIL_URL_TEMPLATE, mId);
    }

    public static class Builder {
        private IssuePost mInstance;

        public Builder() {
            mInstance = new IssuePost();
        }

        public Builder addValue(final Column column, final String text) {
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
