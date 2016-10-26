import model.IssuePost;
import model.IssueThread;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AndroidIssues implements AndroidIssueContract {
    private static class Pagination {
        public int start, end, total;
    }

    // Our base url to fetch issue listings from.
    private static final String BASE_URL_TEMPLATE = "https://code.google.com/p/android/issues/list?can=2&q=assigned" +
            "&colspec=ID+Status+Priority+Owner+Summary+Stars+Reporter+Opened+Component+Type+Version&cells=tiles";

    // Pagination parameters
    private static final String PAGINATION_PARAMS_TEMPLATE = "&num=%d&start=%d";

    // internal CSV file to write issues to
    private static final String CSV_OUTPUT_FILE = "issuelist.csv";

    private static final String PAGINATION_REGEX = ".*\\s*(\\d+)\\s*-\\s*(\\d+)\\s*of\\s*(\\d+).*";

    private static AndroidIssues instance = new AndroidIssues();

    public static AndroidIssues getInstance() {
        return instance;
    }

    private static final int MAX_RESULTS_PER_PAGE = 100;

    private AndroidIssues() {
    }

    @Override
    public List<IssuePost> getIssues() {
        return null;
    }

    @Override
    public List<IssueThread> getIssueDetail(long issueId) {
        return null;
    }

    /**
     * Fetch the first issues listing page from Google.
     *
     * @return Document
     * @throws IOException
     */
    private Document downloadIssuesPage() throws IOException {
        return downloadIssuesPage(0);
    }

    /**
     * Fetch some offset/paginated issues listing page from Google with 100 maximum results.
     *
     * @param offset
     * @return Document
     * @throws IOException
     */
    private Document downloadIssuesPage(int offset) throws IOException {
        return downloadIssuesPage(offset, MAX_RESULTS_PER_PAGE);
    }

    /**
     * Fetch a certain paginated issues listing page from Google with a given number of results.
     *
     * @param offset
     * @param numResults
     * @return Document
     * @throws IOException
     */
    private Document downloadIssuesPage(int offset, int numResults) throws IOException {
        String url = BASE_URL_TEMPLATE + String.format(PAGINATION_PARAMS_TEMPLATE, offset, numResults);
        return Jsoup.connect(url).get();
    }

    /**
     * Fetch the pagination information from the current page.
     *
     * @param document
     * @return Pagination
     */
    private static Pagination getPagination(@NotNull Document document) {
        Element paginationDiv = document.select("div.pagination").first();
        if (paginationDiv == null) {
            throw new IllegalStateException("Pagination Div missing from document");
        }
        String text = paginationDiv.text();
        Pattern regex = Pattern.compile(PAGINATION_REGEX);
        Matcher matcher = regex.matcher(text);
        if (!matcher.matches() || matcher.groupCount() != 3) {
            throw new IllegalStateException("Pagination not formatted correctly");
        }

        Pagination pagination = new Pagination();
        pagination.start = Integer.parseInt(matcher.group(1));
        pagination.end = Integer.parseInt(matcher.group(2));
        pagination.total = Integer.parseInt(matcher.group(3));
        return pagination;
    }

    private static Document scrapeIssuesFromDocument(@NotNull Document document) {
        Element listingTable = document.select("table[id=resultstable]").first();
    }
}
