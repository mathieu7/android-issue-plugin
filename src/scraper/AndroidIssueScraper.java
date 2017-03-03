package scraper;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import model.ColumnValues;
import model.IssueComment;
import model.IssuePost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import settings.UserOptions;
import settings.UserSettings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AndroidIssueScraper {
    private static final Log LOG = LogFactory.getLog(AndroidIssueScraper.class);

    // Pagination parameters
    private static final String PAGINATION_PARAMS_TEMPLATE = "&num=%d&start=%d";
    private static final String PAGINATION_REGEX = ".*\\s*(\\d+)\\s*-\\s*(\\d+)\\s*of\\s*(\\d+).*";
    private static final int MAX_RESULTS_PER_PAGE = 100;
    private static int sRetries;
    private static AndroidIssueScraper instance = new AndroidIssueScraper();

    private AndroidIssueScraper() {
    }

    public static AndroidIssueScraper getInstance(final @NotNull Project project) {
        UserSettings settings = UserSettings.getInstance(project);
        sRetries = settings.getState().getNumberOfRetries();
        return instance;
    }

    /**
     * Fetch the pagination information from the current page.
     *
     * @param document
     * @return Pagination
     */
    private static Pagination getPagination(@NotNull final Document document) {
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
    private static ArrayList<IssuePost> scrapeIssuesFromDocument(@NotNull final Document document) {
        Element listingTable = document.select("table[id=resultstable]").first();
        ArrayList<IssuePost> issueList = new ArrayList<>();

        ArrayList<String> availableHeaders = new ArrayList<>();
        // Get the available columns.
        Elements headers = listingTable.getElementsByTag("th");
        for (Element header : headers) {
            String text = header.text().replaceAll("&nbsp;", " ");
            for (String v : ColumnValues.FULL_COLUMN_SPEC) {
                if (text.contains(v)) {
                    availableHeaders.add(v);
                    break;
                }
            }
        }
        Elements rows = listingTable.getElementsByTag("tr");
        for (Element row : rows) {
            Elements columns = row.select("td[class~=.*col_\\d+");
            if (columns == null || columns.isEmpty()) continue;
            IssuePost post  = new IssuePost();

            for (int index = 0, i=0; index < availableHeaders.size(); index++) {
                String headerKey = availableHeaders.get(i);
                Element column = columns.get(index);
                String text = column.text().replaceAll("&nbsp;", " ");
                if (!text.trim().isEmpty() && text.trim().charAt(0) != NON_BREAKING_SPACE) {
                    post.setValue(headerKey, text);
                    i++;
                }
            }
            issueList.add(post);
        }
        return issueList;
    }

    private static final char NON_BREAKING_SPACE = 160;
    /**
     * Scrape issue details from details page.
     * @param document The document the method is scraping from.
     * @return list of IssueComment
     */
    private static ArrayList<IssueComment> scrapeDetailFromDocument(@NotNull final Document document) {
        Element table = document.select("table[class=issuepage]").first();
        Elements thread = table.select("div[id~=hc\\d+$]");
        ArrayList<IssueComment> issueComments = new ArrayList<>();
        for (Element post : thread) {
            Element dateField = post.select("span[class=date]").first();
            String date = dateField != null ? dateField.text() : "N/A";

            Element authorField = post.select("a[class=userlink]").first();
            String author = authorField != null ? authorField.text() : "N/A";

            String comment = post.select("pre").text();
            issueComments.add(new IssueComment(date, author, comment));
        }
        return issueComments;
    }

    /**
     * Fetch issues from Google.
     * @param progressIndicator
     * @return
     * @throws IssueScraperException
     */
    public List<IssuePost> getIssues(ProgressIndicator progressIndicator)
            throws IssueScraperException {
        Document doc = downloadIssuesPage();
        Pagination pagination = getPagination(doc);
        ArrayList<IssuePost> issues = scrapeIssuesFromDocument(doc);
        while (pagination.start < pagination.end && pagination.end < pagination.total) {
            doc = downloadIssuesPage(pagination.end);
            issues.addAll(scrapeIssuesFromDocument(doc));
            pagination = getPagination(doc);
            progressIndicator.setFraction(
                    (float) issues.size() / (float) pagination.total);
            progressIndicator.setText2("("+ issues.size() + " out of " + pagination.total + ")");
        }
        return issues;
    }

    /**
     * Fetch the issue detail thread from Google
     * @param issue
     * @return
     * @throws IssueScraperException
     */
    public List<IssueComment> getIssueDetail(final IssuePost issue)
            throws IssueScraperException {
        String url = issue.getDetailURL();
        Document doc = fetchFromUrl(url);
        return scrapeDetailFromDocument(doc);
    }

    /**
     * Fetch the first issues listing page from Google.
     *
     * @return Document
     * @throws IOException
     */
    private Document downloadIssuesPage() throws IssueScraperException {
        return downloadIssuesPage(0);
    }

    /**
     * Fetch some offset/paginated issues listing page from Google with 100 maximum results.
     *
     * @param offset
     * @return Document
     * @throws IOException
     */
    private Document downloadIssuesPage(final int offset) throws IssueScraperException {
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
    private Document downloadIssuesPage(final int offset,
                                        final int numResults)
            throws IssueScraperException {
        try {
            String url = URLBuilder.generateIssuesURL()
                    + String.format(PAGINATION_PARAMS_TEMPLATE, numResults, offset);
            return fetchFromUrl(url);
        } catch (URISyntaxException exception) {
            throw new IssueScraperException(exception.getLocalizedMessage());
        }
    }

    private Document fetchFromUrl(final String url) throws IssueScraperException {
        int retries = 0;
        while (retries < sRetries) {
            try {
                return Jsoup.connect(url).get();
            } catch (Exception ex) {
                ex.printStackTrace();
                LOG.debug("Retrying "+ retries + "/"+ sRetries);
            }
            retries++;
        }
        throw new IssueScraperException("Could not fetch from "+ url);
    }

    /**
     * Represents a pagination for Google Issue Tracker.
     */
    private static class Pagination {
        public int start, end, total;
    }

    /**
     * Custom general exception
     */
    public static class IssueScraperException extends Exception {
        IssueScraperException(final String message) { super(message); }
    }

    /**
     * Helper class to generate Google Android Issue website URLs.
     */
    private static class URLBuilder {
        private static final String SCHEME = "https";
        private static final String HOST = "code.google.com";
        private static final String PATH = "/p/android/issues/list";

        /**
         * Method to generate a Google Issues URL depending on user settings.
         * @return String
         * @throws URISyntaxException
         */
        public static String generateIssuesURL()
                throws URISyntaxException {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(SCHEME)
                    .setHost(HOST)
                    .setPath(PATH)
                    .addParameter("can", "2")
                    //TODO: This should be whatever the user filters for.
                    .addParameter("q", "status=assigned")
                    .addParameter("colspec",
                            StringUtils.join(ColumnValues.FULL_COLUMN_SPEC, " "));
            return builder.build().toString();
        }
    }
}
