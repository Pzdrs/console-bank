package bohac.ui;

import bohac.util.Utils;

import java.util.AbstractMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.function.Function;

import static bohac.ui.TerminalSession.SCANNER;

public class TerminalUtils {
    private TerminalUtils() {
        throw new AssertionError();
    }

    public static int promptNumericInt(String message, Map.Entry<Integer, Integer> range) {
        boolean error = true;
        int value = 0;
        do {
            try {
                System.out.print(message);
                value = SCANNER.nextInt();
                if (!Utils.inRange(value, range.getKey(), range.getValue())) {
                    System.out.println("Value out of range");
                    continue;
                }
                error = false;
            } catch (InputMismatchException exception) {
                SCANNER.next();
                System.out.println("Not a number");
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
    public static double promptNumericDouble(String message) {
        boolean error = true;
        double value = 0;
        do {
            try {
                System.out.print(message);
                value = SCANNER.nextDouble();
                error = false;
            } catch (InputMismatchException exception) {
                SCANNER.next();
                System.out.println("Not a number");
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
    public static int promptNumericInt(String message) {
        return promptNumericInt(message, new AbstractMap.SimpleEntry<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    public static String ws(int amount) {
        return " ".repeat(amount);
    }

    public static String nl(int amount) {
        return "\n".repeat(amount);
    }

    public static String repeat(String s, int amount) {
        return s.repeat(amount);
    }

    public static void clear() {
        System.out.println(nl(50));
    }

    public static String getSectionHeader(String headerText) {
        String padding = ws(15);
        String headerContent = String.format("%s>>> %s <<<%s\n", padding, headerText, padding);
        return headerContent + repeat("=", headerContent.length() - 1);
    }

    public static int printHeaderAndGetWidth(String headerText) {
        String sectionHeader = getSectionHeader(headerText);
        System.out.println(sectionHeader);
        return sectionHeader.length() / 2;
    }

    public static String center(String content, String relativeTo) {
        int padding = Math.max(content.length(), relativeTo.length());
        return ws(padding / 2) + content + ws(padding / 2);
    }

    public static String center(String content, int relativeTo) {
        int padding = Math.abs(content.length() - relativeTo);
        return ws(padding / 2) + content + ws(padding / 2);
    }

    public static Object chooseOne(Object[] objects, Function<Object, String> objectDisplayName) {
        System.out.println("Choose one: ");
        for (int i = 0; i < objects.length; i++) {
            System.out.printf("[%d] %s\n", i + 1, objectDisplayName.apply(objects[i]));
        }
        return objects[TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(1, objects.length)) - 1];
    }

    public static String minimize(String s, int amount) {
        return s.substring(0, amount) + "..." + s.substring(s.length() - amount);
    }
}
