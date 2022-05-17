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

public class LanguageManager {
    public static final Set<Locale> SUPPORTED_LANGUAGES = Set.of(
            Locale.ENGLISH,
            new Locale("cz")
    );
    private static LanguageManager INSTANCE;
    private Locale locale;
    private Map<String, Object> data;

    private LanguageManager() {

    }

    public static LanguageManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LanguageManager();
        }
        return INSTANCE;
    }

    public Locale getLocale() {
        return locale;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getString(String key) {
        return String.valueOf(data.get(key));
    }

    public String getString(String key, String placeholder, String value) {
        return getString(key, Map.of(placeholder, value));
    }

    public String getString(String key, Map<String, String> placeholders) {
        String string = getString(key);
        for (Map.Entry<String, String> variable : placeholders.entrySet()) {
            string = string.replace(String.format("{%s}", variable.getKey()), variable.getValue());
        }
        return string;
    }

    public void setLocale(Locale locale) {
        checkSupported(locale);
        this.locale = locale;
        load(Path.of(Configuration.DATA_ROOT, Configuration.LOCALE_ROOT, locale.toLanguageTag() + ".yaml"));
    }

    private void checkSupported(Locale locale) {
        if (!LanguageManager.SUPPORTED_LANGUAGES.contains(locale)) throw new LanguageNotSupportedException(locale);
    }

    private void load(Path path) {
        File file = path.toFile();
        try {
            this.data = new Yaml().load(new FileInputStream(path.toFile()));
            Utils.printDebugMessage("debug_language_load", "locale", locale.toLanguageTag());
        } catch (FileNotFoundException e) {
            throw new LanguageNotFoundException(file);
        }
    }
}
