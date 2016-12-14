package scraper;

import model.IssuePost;
import model.IssueThread;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidIssues {
    private static class Pagination {
        int start, end, total;
    }

    // Our base url to fetch issue listings from.
    private static final String BASE_URL_TEMPLATE = "https://code.google.com/p/android/issues/list?can=2&q=assigned" +
            "&colspec=ID%20Status%20Priority%20Owner%20Summary%20Stars%20Reporter%20Opened%20Component%20Type%20Version&cells=tiles";

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

    private static final String DETAIL_URL_TEMPLATE = "https://code.google.com/p/android/issues/detail?id=%d";

    private AndroidIssues() {
    }

    public List<IssuePost> getIssues() throws IOException {
        Document doc = downloadIssuesPage();
        Pagination pagination = getPagination(doc);
        ArrayList<IssuePost> issues = scrapeIssuesFromDocument(doc);
        /*while (pagination.start < pagination.end && pagination.end < pagination.total) {
            doc = downloadIssuesPage(pagination.end);
            issues.addAll(scrapeIssuesFromDocument(doc));
            pagination = getPagination(doc);
            System.out.println("Fetching: " + pagination.end + " out of " + pagination.total);
        }*/
        return issues;
    }

    public List<IssueThread> getIssueDetail(long issueId) throws IOException {
        Document doc = Jsoup.connect(String.format(DETAIL_URL_TEMPLATE, issueId)).get();
        return scrapeDetailFromDocument(doc);
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
        String url = BASE_URL_TEMPLATE + String.format(PAGINATION_PARAMS_TEMPLATE, numResults, offset);
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

    /**
     * Fetch a list of issues from a document.
     * @param document
     * @return list of IssuePosts
     */
    private static ArrayList<IssuePost> scrapeIssuesFromDocument(@NotNull Document document) {
        Element listingTable = document.select("table[id=resultstable]").first();
        ArrayList<IssuePost> issueList = new ArrayList<>();
        Elements rows = listingTable.getElementsByTag("tr");
        for (Element row : rows) {
            Elements columns = row.select("td[class~=.*col_\\d+");
            if (columns == null || columns.isEmpty()) continue;
            IssuePost.Builder builder = new IssuePost.Builder();
            for (int index = 0, i = 0; index < IssuePost.Column.values().length; index++) {
                Element column = columns.get(index);
                String text = column.text().replaceAll("&nbsp;", " ");
                if (!text.trim().isEmpty() && text.trim().charAt(0) != 160) {
                    builder.addValue(IssuePost.Column.values()[i], text);
                    i++;
                }
            }
            issueList.add(builder.build());
        }
        return issueList;
    }

    private static ArrayList<IssueThread> scrapeDetailFromDocument(@NotNull Document document) {
        Element table = document.select("table[class=issuepage]").first();
        Elements thread = table.select("div[id~=hc\\d+$]");
        ArrayList<IssueThread> issueThreads = new ArrayList<>();
        for (Element post : thread) {
            String date = post.select("span[class=date]").first().text();
            String author = post.select("a[class=userlink").first().text();
            String comment = post.select("pre").text();
            issueThreads.add(new IssueThread(date, author, comment));
        }
        return issueThreads;
    }
}
