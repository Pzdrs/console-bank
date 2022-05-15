package bohac;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Configuration {
    public static boolean DEBUG = true;
    public static String DATA_ROOT = "data";
    public static String LOCALE_ROOT = "lang";
    public static final int MAX_LOGIN_TRIES = 5;
    public static final int USER_MAX_ACCOUNTS = 5;
    public static final Duration LOGIN_TIMEOUT_DURATION = Duration.of(30, ChronoUnit.SECONDS);
    public static final Locale DEFAULT_LANGUAGE = Locale.ENGLISH;
}
