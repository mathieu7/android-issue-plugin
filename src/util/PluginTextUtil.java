package util;

import java.util.ResourceBundle;

/**
 * Helper class to load localized strings.
 */
public class PluginTextUtil {
    private static final String BUNDLE_NAME = "plugin-text";

    /**
     * Find a resource string by key in the default locale.
     * @param key
     * @return
     */
    public static String getString(final String key) {
        return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
    }
}
