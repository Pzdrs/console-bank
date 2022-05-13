package bohac.ui;

import bohac.Utils;

import java.util.AbstractMap;
import java.util.InputMismatchException;
import java.util.Map;

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
}
