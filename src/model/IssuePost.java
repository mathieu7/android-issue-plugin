package model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class representing an android issue listing.
 */
public class IssuePost implements Serializable {
    private static final String DETAIL_URL_TEMPLATE =
            "https://code.google.com/p/android/issues/detail?id=%s";

    private HashMap<String, String> mValueMap = new HashMap<>();

    public String[] getAsArray() {
        return new String[] {
        };
    }

    public final String getId() { return mValueMap.get(ColumnValues.ID); }
    public final String getDetailURL() {
        return String.format(DETAIL_URL_TEMPLATE, getId());
    }

    public void setValue(final String key, final String value) {
        mValueMap.put(key, value);
    }

}
