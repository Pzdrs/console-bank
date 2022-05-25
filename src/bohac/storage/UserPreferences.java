package bohac.storage;

import org.json.JSONObject;

import java.util.Properties;

/**
 * Stores per-user preferences. Extends from {@link Properties} class.
 */
public class UserPreferences extends Properties implements JSONSerializable {
    /**
     * Loader method
     *
     * @param object JSON object
     * @return a new instance of {@link UserPreferences}
     */
    public static UserPreferences load(JSONObject object) {
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.putAll(object.toMap());
        return userPreferences;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        forEach((key, value) -> object.put(key.toString(), value));
        return object;
    }
}
