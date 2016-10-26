package model;

/**
 * Pojo representing an issue's thread.
 */
public class IssueThread {
    private String mAuthor;
    private String mDate;
    private String mComment;

    public IssueThread(String author, String date, String comment) {
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
