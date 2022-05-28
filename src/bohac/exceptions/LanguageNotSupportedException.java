package bohac.exceptions;

import java.util.Locale;

/**
 * This exception is thrown, when the program tries to load an unsupported language
 */
public class LanguageNotSupportedException extends RuntimeException {
    public LanguageNotSupportedException(Locale locale) {
        super(String.format("Language '%s' is not supported", locale));
    }
}
