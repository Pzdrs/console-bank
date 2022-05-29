package bohac.ui;

import bohac.Configuration;
import bohac.entity.account.Account;
import bohac.util.Utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static bohac.ui.TerminalSession.SCANNER;
import static bohac.ui.TerminalSession.LANGUAGE_MANAGER;

public class TerminalUtils {
    private TerminalUtils() {
        throw new AssertionError();
    }

    /**
     * Prompts the user for a string
     *
     * @param message message
     * @return string
     */
    public static String promptString(String message) {
        System.out.print(message + ": ");
        return SCANNER.nextLine();
    }

    /**
     * Prompts the user for a string - rejects empty inputs
     *
     * @param message         message
     * @param languageManager language manager instance
     * @return non empty string
     */
    public static String promptStringMandatory(String message, LanguageManager languageManager) {
        return promptStringValidated(message, s -> !s.isBlank(), () -> System.out.println(languageManager.getString("string_empty")));
    }

    /**
     * Prompts the user for a string and validates it using the supplied {@code Predicate}
     *
     * @param message   message
     * @param predicate predicate that determines, whether the user is prompted again, or the value is accepted
     * @param invalid   what happens every time the input value is invalid
     * @return the validated string
     */
    public static String promptStringValidated(String message, Predicate<String> predicate, Runnable invalid) {
        String s;
        do {
            s = promptString(message);
            if (!predicate.test(s)) invalid.run();
        } while (!predicate.test(s));
        return s;
    }

    /**
     * Prompt the user for and integer - bounded from left and right
     *
     * @param message message
     * @param range   bounds
     * @return integer
     */
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
     * @param objects                     the array the user chooses from
     * @param objectDisplayNameDescriptor how should each of the objects be represented in the choice list,
     *                                    if null is passed in, the object's {@link Object#toString()} method is used
     * @return the object from the array that the user has chosen
     */
    public static <T> T chooseOne(T[] objects, Function<T, String> objectDisplayNameDescriptor) {
        System.out.printf("%s:%n", LANGUAGE_MANAGER.getString("menu_choose_one"));
        for (int i = 0; i < objects.length; i++) {
            System.out.printf("[%d] %s\n", i + 1, objectDisplayNameDescriptor == null ? objects[i] : objectDisplayNameDescriptor.apply(objects[i]));
        }
        int choice = TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(0, objects.length), TerminalSession.LANGUAGE_MANAGER);
        if (choice == 0) return null;
        return objects[choice - 1];
    }

    /**
     * Does the same thing as {@code chooseOne(Object[], Function<Object, String>)}, also takes in a {@code Consumer<Object>}
     * that defines what to do with the chosen object - developer friendly bohac.util.API
     *
     * @param objects                     the array the user chooses from
     * @param objectDisplayNameDescriptor how should each of the objects be represented in the choice list,
     *                                    if null is passed in, the object's {@link Object#toString()} method is used
     * @param chosenObject                defines what to do with the chosen object
     */
    public static <T> void chooseOne(T[] objects, Function<T, String> objectDisplayNameDescriptor, Consumer<T> chosenObject) {
        T o = chooseOne(objects, objectDisplayNameDescriptor);
        if (o != null) chosenObject.accept(o);
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

    /**
     * @param accounts user's accounts
     * @return the accounts overview
     */
    public static String getAccountsOverview(List<Account> accounts) {
        return TerminalSession.LANGUAGE_MANAGER.getString("menu_accounts_overview", Map.of(
                "accounts", accounts.size(),
                "slotsLeft", Configuration.USER_MAX_ACCOUNTS - accounts.size()
        )) + "\n";
    }

    /**
     * Displays a set of objects in an in order fashion. All messages are passed in as a language key and later resolved by the {@link LanguageManager}
     *
     * @param dataSet               data
     * @param displayNameDescriptor defines what each record will display as
     * @param order                 instance of {@link Comparator<T>} describing the order
     * @param orderLabel            order label
     * @param message               message (i.e Showing n objects (order))
     * @param <T>                   type
     */
    public static <T extends Comparable<T>> void showSet(List<T> dataSet,
                                                         Function<T, String> displayNameDescriptor,
                                                         Comparator<T> order,
                                                         String orderLabel, String message) {
        System.out.println(LANGUAGE_MANAGER.getString(message,
                Map.of(
                        "count", dataSet.size(),
                        "order", LANGUAGE_MANAGER.getString(orderLabel)
                )));
        System.out.println();
        if (order == null) Collections.sort(dataSet);
        else dataSet.sort(order);
        dataSet.forEach(data -> System.out.println(displayNameDescriptor == null ? data : displayNameDescriptor.apply(data)));
        Menu.BACK_ONLY.prompt();
    }
}
