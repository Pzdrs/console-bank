package bohac;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class Configuration {
    public static boolean DEBUG = true;
    public static Path DATA_ROOT = Path.of("data");
    public static final int MAX_LOGIN_TRIES = 5;
    public static final Duration LOGIN_TIMEOUT_DURATION = Duration.of(30, ChronoUnit.SECONDS);
    public static final List<Locale> SUPPORTED_LANGUAGES = List.of(
            new Locale("en", "US"),
            new Locale("cz", "CS")
    );
    public static final Locale DEFAULT_LANGUAGE = SUPPORTED_LANGUAGES.get(0);
}
