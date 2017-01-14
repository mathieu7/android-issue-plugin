package model;

/**
 * Bean representing an issue's thread.
 */
public final class IssueComment {
    private String mAuthor;
    private String mDate;
    private String mComment;

    public IssueComment(final String author,
                        final String date,
                        final String comment) {
        mAuthor = author;
        mDate = date;
        mComment = comment;
    }

    public String getAuthor() {
        return mAuthor;
    }
    public String getDate() {
        return mDate;
    }
    public String getComment() {
        return mComment;
    }
}
