package bohac.ui;

import bohac.util.Utils;

import java.util.AbstractMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.function.Function;

import static bohac.ui.TerminalSession.SCANNER;
import static bohac.ui.TerminalSession.languageManager;

public class TerminalUtils {
    private TerminalUtils() {
        throw new AssertionError();
    }

    public static int promptNumericInt(String message, Map.Entry<Integer, Integer> range, LanguageManager languageManager) {
        boolean error = true;
        int value = 0;
        do {
            try {
                System.out.print(message);
                value = SCANNER.nextInt();
                if (!Utils.inRange(value, range.getKey(), range.getValue())) {
                    System.out.println(languageManager.getString("error_value_out_of_range"));
                    continue;
                }
                error = false;
            } catch (InputMismatchException exception) {
                SCANNER.next();
                System.out.println(languageManager.getString("error_nan"));
            }
        } while (error);
        SCANNER.nextLine();
        return value;
    }

    /**
     * Prompt the user for a double
     *
     * @param message Prompt message
     * @return Validated integer
     */
    public static double promptNumericDouble(String message, LanguageManager languageManager) {
        boolean error = true;
        double value = 0;
        do {
            try {
                System.out.print(message);
                value = SCANNER.nextDouble();
                error = false;
            } catch (InputMismatchException exception) {
                SCANNER.next();
                System.out.println(languageManager.getString("error_nan"));
            }
        } while (error);
        SCANNER.nextLine();
        return value;
    }

    /**
     * Prompt the user for and integer - the whole domain is considered (no upper or lower limits)
     *
     * @param message Prompt message
     * @return Integer
     */
    public static int promptNumericInt(String message, LanguageManager languageManager) {
        return promptNumericInt(message, new AbstractMap.SimpleEntry<>(Integer.MIN_VALUE, Integer.MAX_VALUE), languageManager);
    }

    /**
     * Generates a whitespace
     *
     * @param amount how many whitespaces
     * @return variable length whitespace
     */
    public static String whiteSpace(int amount) {
        return " ".repeat(amount);
    }

    /**
     * Clears the console
     */
    public static void clear() {
        System.out.println("\n".repeat(50));
    }

    /**
     * Formats a given text to a menu section header
     *
     * @param headerText header text
     * @return formatted menu section header
     */
    public static String getSectionHeader(String headerText) {
        String padding = whiteSpace(15);
        String headerContent = String.format("%s>>> %s <<<%s\n", padding, headerText, padding);
        return headerContent + "=".repeat(headerContent.length() - 1);
    }

    /**
     * Prints a given string as a formatted menu section header using the {@link TerminalUtils#getSectionHeader(String)}
     * and returns its total length. Best used in conjunction with {@link TerminalUtils#center(String, int)}
     *
     * @param headerText header text
     * @return width of the header
     */
    public static int printHeaderAndGetWidth(String headerText) {
        String sectionHeader = getSectionHeader(headerText);
        System.out.println(sectionHeader);
        return sectionHeader.length() / 2;
    }

    /**
     * Centers - adds white spaces to both sides - a string relative to another string
     *
     * @param content    string to center
     * @param relativeTo what to center relative to
     * @return centered string
     */
    public static String center(String content, String relativeTo) {
        int padding = Math.abs(content.length() - relativeTo.length());
        return whiteSpace(padding / 2) + content + whiteSpace(padding / 2);
    }

    /**
     * Does the same thing as {@link TerminalUtils#center(String, String)}, except a number is
     * passed in representing the other string's length
     *
     * @param content    string to center
     * @param relativeTo what to center relative to
     * @return centered string
     */
    public static String center(String content, int relativeTo) {
        int padding = Math.abs(content.length() - relativeTo);
        return whiteSpace(padding / 2) + content + whiteSpace(padding / 2);
    }

    /**
     * If a user needs to choose from an array of things - Objects - this method is used
     *
     * @param objects           the array the user chooses from
     * @param objectDisplayName how should each of the objects be represented in the choice list,
     *                          if null is passed in, the object's {@link Object#toString()} method is used
     * @return the object from the array that the user has chosen
     */
    public static Object chooseOne(Object[] objects, Function<Object, String> objectDisplayName) {
        System.out.printf("%s:%n", languageManager.getString("menu_choose_one"));
        for (int i = 0; i < objects.length; i++) {
            System.out.printf("[%d] %s\n", i + 1, objectDisplayName == null ? objects[i] : objectDisplayName.apply(objects[i]));
        }
        return objects[TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(1, objects.length),
                TerminalSession.languageManager) - 1];
    }

    /**
     * If a string is too long, this method can remove the majority of it from the middle
     *
     * @param s      string
     * @param amount how much to leave on both sides
     * @return minimized string
     */
    public static String minimize(String s, int amount) {
        return s.substring(0, amount) + "..." + s.substring(s.length() - amount);
    }
}
