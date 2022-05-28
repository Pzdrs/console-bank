package bohac.storage;

import bohac.entity.User;
import bohac.ui.LanguageManager;
import bohac.util.Utils;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Properties;

/**
 * Stores per-user preferences. Extends from {@link Properties} class.
 */
public class UserPreferences extends Properties implements JSONSerializable {
    /**
     * Set's a new preferred language for this user
     *
     * @param locale the new preferred language
     */
    public void setPreferredLanguage(Locale locale) {
        setProperty("locale", locale.toString());
    }

    /**
     * @return the user's preferred language, if none is set for this user, the current language is returned
     */
    public Locale getPreferredLanguage() {
        String locale = getProperty("locale");
        if (locale == null) return LanguageManager.getInstance().getLocale();
        String[] s = locale.split("_");
        return new Locale(s[0], s[1]);
    }

    /**
     * @param locale language to compare
     * @return true, if the passed locale is the same as the user's preferred language, false otherwise
     */
    public boolean getPreferredLanguage(Locale locale) {
        return getPreferredLanguage().getLanguage().equals(locale.getLanguage());
    }


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
