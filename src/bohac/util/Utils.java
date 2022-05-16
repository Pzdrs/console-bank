package bohac.util;

import bohac.Configuration;
import bohac.entity.Account;
import bohac.entity.User;
import bohac.storage.JSONSerializable;
import bohac.ui.TerminalSession;
import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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

    public static String getDefaultAccountName(User user, Account.Type type) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s's ", user.getFullName()));
        builder.append(type.name().replace("_", " ").toLowerCase());
        return builder.toString();
    }

    public static void loadFile(File file, Consumer<JSONArray> consumer, Consumer<JSONArray> createDefaults, List<? extends JSONSerializable> defaults) {
        if (!file.exists()) {
            Utils.printDebugMessage("debug_file_default", "file", String.valueOf(file));
            JSONArray defaultAccounts = new JSONArray();
            defaults.forEach(account -> defaultAccounts.put(account.toJSON()));
            createDefaults.accept(defaultAccounts);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            consumer.accept(array);
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

    public static void printDebugMessage(String key) {
        if (Configuration.DEBUG) System.out.println(TerminalSession.languageManager.getString(key));
    }

    public static void printDebugMessage(String key, String placeholder, String value) {
        if (Configuration.DEBUG) System.out.println(TerminalSession.languageManager.getString(key, placeholder, value));
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
}
