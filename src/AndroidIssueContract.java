import model.IssuePost;
import model.IssueThread;

import java.util.List;

interface AndroidIssueContract {
    List<IssuePost> getIssues();
    List<IssueThread> getIssueDetail(long issueId);
}
