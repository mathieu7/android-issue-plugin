import model.IssuePost;
import model.IssueThread;

import java.io.IOException;
import java.util.List;

interface AndroidIssueContract {
    List<IssuePost> getIssues() throws IOException;
    List<IssueThread> getIssueDetail(long issueId) throws IOException;
}
