package bohac.ui;

import bohac.Configuration;
import bohac.exceptions.LanguageNotFoundException;
import bohac.exceptions.LanguageNotSupportedException;
import bohac.util.Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;

/**
 * The {@code LanguageManager} keeps track of all language related things.
 */
public class LanguageManager {
    /**
     * This {@code Set<Locale>} defines all the languages this program is capable of using
     */
    public static final Set<Locale> SUPPORTED_LANGUAGES = Set.of(
            new Locale("en", "US"),
            new Locale("cs", "CZ")
    );
    private static LanguageManager INSTANCE;
    /**
     * The current {@code Locale}
     */
    private Locale locale;
    /**
     * The data loaded from disk according to the {@code locale} attribute
     */
    private Map<String, Object> data;

    private LanguageManager() {

    }

    public static LanguageManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LanguageManager();
        }
        return INSTANCE;
    }

    /**
     * @param key message key
     * @return the value associated with the passed in key
     */
    public String getString(String key) {
        return String.valueOf(data.get(key));
    }

    /**
     * @param key         message key
     * @param placeholder placeholder key
     * @param value       placeholder value
     * @return the value associated with the passed in key with the single variable injected in accordingly
     */
    public String getString(String key, String placeholder, String value) {
        return getString(key, Map.of(placeholder, value));
    }

    /**
     * Does the same thing as {@code getString(String, String, String)}, but the placeholder value is a number
     */
    public String getString(String key, String placeholder, int value) {
        return getString(key, placeholder, String.valueOf(value));
    }

    /**
     * @param key          message key
     * @param placeholders the placeholder map - variable amount of placeholders
     * @return the value associated with the passed in key with all the variables injected in accordingly
     */
    public String getString(String key, Map<String, Object> placeholders) {
        String string = getString(key);
        for (Map.Entry<String, Object> variable : placeholders.entrySet()) {
            string = string.replace(String.format("{%s}", variable.getKey()), variable.getValue().toString());
        }
        return string;
    }

    /**
     * Set the locale for the current session
     *
     * @param locale locale
     */
    public void setLocale(Locale locale) {
        checkSupported(locale);
        this.locale = locale;
        load(Path.of(Configuration.DATA_ROOT, Configuration.LOCALE_ROOT, locale + ".yaml"));
    }

    /**
     * Checks if a locale is supported
     *
     * @param locale locale
     * @throws LanguageNotSupportedException if the passed in locale is not supported
     */
    private void checkSupported(Locale locale) {
        if (!LanguageManager.SUPPORTED_LANGUAGES.contains(locale)) throw new LanguageNotSupportedException(locale);
    }

    /**
     * Loads the language data from a .yaml file and parses it to a {@code Map<String, Object>}
     *
     * @param path file path
     */
    private void load(Path path) {
        File file = path.toFile();
        try {
            this.data = new Yaml().load(new FileInputStream(path.toFile()));
            Utils.printDebugMessage(String.format("Language loaded, using %s", locale));
        } catch (FileNotFoundException e) {
            throw new LanguageNotFoundException(file);
        }
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Basically outsourcing the exceptions handling of the {@link Currency#getInstance(Locale)}
     *
     * @param locale locale
     * @return an instance of {@link Currency}, if invalid locale is passed in, null is returned
     */
    public static Currency getCurrency(Locale locale) {
        try {
            return Currency.getInstance(locale);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Basically outsourcing the exceptions handling of the {@link Currency#getInstance(String)}
     *
     * @param code string representation of {@link Locale}
     * @return an instance of {@link Currency}, if invalid locale is passed in, null is returned
     */
    public static Currency getCurrency(String code) {
        try {
            return Currency.getInstance(code);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
