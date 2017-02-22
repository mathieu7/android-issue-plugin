package scraper;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import settings.AndroidIssueTrackerOptions;

import java.net.URISyntaxException;

/**
 * Helper class to generate Google Android Issue website URLs.
 */
class URLBuilder {
    private static final String SCHEME = "https";
    private static final String HOST = "code.google.com";
    private static final String PATH = "/p/android/issues/list";

    /**
     * Method to generate a Google Issues URL depending on user settings.
     * @param options
     * @return String
     * @throws URISyntaxException
     */
    public static String generateIssuesURL(final AndroidIssueTrackerOptions options)
            throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(SCHEME)
                .setHost(HOST)
                .setPath(PATH)
                .addParameter("can", "2")
                //TODO: This should be whatever the user filters for.
                .addParameter("q", "status=assigned")
                .addParameter("colspec",
                        StringUtils.join(options.getSelectedColumnSpecs(), " "));
        return builder.build().toString();
    }
}
