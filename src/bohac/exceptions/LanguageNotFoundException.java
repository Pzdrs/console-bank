package bohac.exceptions;

import java.io.File;

public class LanguageNotFoundException extends RuntimeException {
    public LanguageNotFoundException(File file) {
        super(String.format("Language file %s not found", file));
    }
}
