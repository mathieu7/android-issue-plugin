package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing an android issue listing.
 */
public class IssuePost implements Serializable {
    private static final String DETAIL_URL_TEMPLATE =
            "https://code.google.com/p/android/issues/detail?id=%s";

    private HashMap<String, String> mValueMap = new HashMap<>();

    public String[] getAsArray(final String[] properties) {
        ArrayList<String> ret = new ArrayList<>();
        for (String property : properties) {
            if (mValueMap.containsKey(property)) {
                ret.add(mValueMap.get(property));
            }
        }
        String[] retArray = new String[ret.size()];
        ret.toArray(retArray);
        return retArray;
    }

    /**
     * Get the ID associated with the post.
     * @return
     */
    public final String getId() { return mValueMap.get(ColumnValues.ID); }

    /**
     * Get a formatted Google Detail URL
     * @return
     */
    public final String getDetailURL() {
        return String.format(DETAIL_URL_TEMPLATE, getId());
    }

    public void setValue(final String key, final String value) {
        mValueMap.put(key, value);
    }

}
