package bohac.exceptions;

/**
 * This exception is thrown when an essential API is not available
 */
public class ApiNotAvailableException extends RuntimeException {
    public ApiNotAvailableException(String baseURL) {
        super(String.format("The API at %s required by this program is not responding", baseURL));
    }
}
