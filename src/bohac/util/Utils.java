package bohac.util;

import bohac.Configuration;
import bohac.entity.User;
import bohac.entity.account.Account;
import bohac.storage.JSONSerializable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class Utils {
    private Utils() {
        throw new AssertionError();
    }

    /**
     * Check whether a value is in range of two bounds
     *
     * @param value The tested value
     * @param low   Low bound
     * @param high  High bound
     * @return Is the value in range
     */
    public static boolean inRange(int value, int low, int high) {
        return value >= low && value <= high;
    }

    /**
     * Loads a JSON file (JSON array of objects)
     *
     * @param file           loaded file
     * @param consumer       what to do with the array elements (objects)
     */
    public static void loadFile(File file, Consumer<JSONObject> consumer) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JSONObject object = new JSONObject(new JSONTokener(reader));
            consumer.accept(object);
        } catch (IOException e) {
            System.err.printf("Couldn't load accounts from %s%n", file);
        }
    }

    /**
     * Epoch time in seconds to LocalDateTime object
     *
     * @param epoch epoch time in seconds
     * @return the LocalDateTime object
     */
    public static LocalDateTime parseEpoch(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
    }

    /**
     * Converts LocalDateTime object to Epoch time (seconds)
     *
     * @param dateTime date time to convert
     * @return {@code dateTime} in seconds since the Epoch
     */
    public static long toEpoch(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(dateTime));
    }

    /**
     * Just a shorthand version of {@code LocalDateTime.ofLocalizedDateTime(style).format(dateTime)}
     *
     * @param dateTime date time to format
     * @param style    formatting style
     * @return formatted date time
     */
    public static String localizedDateTime(LocalDateTime dateTime, FormatStyle style) {
        return DateTimeFormatter.ofLocalizedDateTime(style).format(dateTime);
    }

    /**
     * If the debug mode is enabled - {@code Configuration.DEBUG} - a passed in message is printed out
     *
     * @param message the debug message
     */
    public static void printDebugMessage(String message) {
        if (Configuration.DEBUG) System.out.println(message);
    }

    /**
     * String similarity check
     *
     * @return 0 to 1 how similar the two given strings are
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    /**
     * Returns the string metric for measuring difference between two sequences
     *
     * @author <a href="http://rosettacode.org/wiki/Levenshtein_distance#Java">...</a>
     */
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    /**
     * Generates a default account name
     *
     * @param type account type
     * @param user account owner
     * @return generated default account name
     */
    public static String getDefaultAccountName(Account.Type type, User user) {
        return String.format("%s's %s", user.getFullName(), type);
    }
}
