package bohac.exceptions;

import java.util.Locale;

public class LanguageNotSupportedException extends RuntimeException {
    public LanguageNotSupportedException(Locale locale) {
        super(String.format("Language '%s' is not supported", locale));
    }
}
