package bohac.entity;

import bohac.JSONSerializable;
import org.json.JSONObject;

import java.util.Properties;

public class UserPreferences extends Properties implements JSONSerializable {
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
