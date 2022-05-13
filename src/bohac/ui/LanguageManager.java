package bohac.ui;

import bohac.Bank;
import bohac.Configuration;
import bohac.Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageManager {
    private static LanguageManager INSTANCE;
    private Locale locale = Configuration.DEFAULT_LANGUAGE;
    private Map<String, Object> data;

    private LanguageManager() {
        this.data = new HashMap<>();
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

    public void load(Path path) {
        try {
            this.data = new Yaml().load(new FileInputStream(path.toFile()));
            System.out.println(Utils.getMessage("debug_language_load").replace("{locale}", locale.toLanguageTag()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
