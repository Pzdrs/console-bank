package bohac.exceptions;

import java.io.File;

/**
 * This exception is thrown, when trying to load a language, which doesn't exist
 */
public class LanguageNotFoundException extends RuntimeException {
    public LanguageNotFoundException(File file) {
        super(String.format("Language file %s not found", file));
    }
}
