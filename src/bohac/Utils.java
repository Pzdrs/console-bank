package bohac;

import bohac.ui.TerminalSession;

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
}
