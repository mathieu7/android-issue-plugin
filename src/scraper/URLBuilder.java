package scraper;

/**
 * Helper class to generate Google Android Issue website URLs.
 */
public class URLBuilder {
    private static final String BASE_URL_TEMPLATE =
            "https://code.google.com/p/android/issues/list?can=2&q=assigned"
                    + "&colspec=ID%20Status%20Priority%20Owner%20Summary"
                    + "%20Stars%20Reporter%20Opened%20Component%20Type%20Version"
                    + "&cells=tiles"; 
}
