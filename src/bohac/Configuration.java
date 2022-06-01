package bohac;

import bohac.entity.account.Balance;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Locale;

/**
 * The global configuration for the entire program
 */
public class Configuration {
    private Configuration() {
        throw new AssertionError();
    }

    /**
     * Debug mode. If set to true, debug messages will be showing up
     */
    public static boolean DEBUG = true;
    /**
     * Path to where all the backend related data is stored
     */
    public static String DATA_ROOT = "data";
    /**
     * Path to where all the language related data is stored
     */
    public static String LOCALE_ROOT = "lang";
    /**
     * How many times can a user enter a wrong password before they are put on timeout
     */
    public static final int MAX_LOGIN_TRIES = 5;
    /**
     * Defines the maximum amount of bank accounts per user
     */
    public static final int USER_MAX_ACCOUNTS = 5;
    /**
     * Defines the duration of a login timeout
     */
    public static final Duration LOGIN_TIMEOUT_DURATION = Duration.of(30, ChronoUnit.SECONDS);
    /**
     * The default language of the program
     */
    public static final Locale DEFAULT_LANGUAGE = Locale.getDefault();

    /**
     * How much is a user charged for making a transaction
     */
    public static final Balance TRANSACTION_FEE = new Balance(Currency.getInstance("CZK"), 10);

    /**
     * The base currency - used for sorting, etc..
     */
    public static final Currency BASE_CURRENCY = Currency.getInstance("EUR");
}
