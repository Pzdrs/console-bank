package bohac;

import bohac.entity.Account;
import bohac.ui.TerminalSession;
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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    private Utils() {
        throw new AssertionError();
    }

    public static String getMessage(String key) {
        return TerminalSession.languageManager.getString(key);
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

    public static void loadFile(File file, Consumer<JSONArray> consumer, Consumer<JSONArray> createDefaults, List<? extends JSONSerializable> defaults) {
        if (!file.exists()) {
            System.out.printf("Accounts file %s doesn't exist, creating defaults..", file);
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

    public static LocalDateTime parseEpoch(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
    }
}
