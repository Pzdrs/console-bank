package bohac.util;

import bohac.exceptions.ApiNotAvailableException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Represents a single REST API
 */
public class Api {
    private final String baseURL;

    public Api(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Send a GET request to this API
     *
     * @param endpoint api endpoint
     * @return instance of {@link InputStream}
     */
    public InputStream get(String endpoint) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseURL + endpoint).openConnection();

            connection.setRequestProperty("accept", "application/json");

            return connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a GET request to this API
     *
     * @param endpoint api endpoint
     * @return parsed response body to {@link JSONObject}
     */
    public JSONObject getJSON(String endpoint) {
        return new JSONObject(new JSONTokener(get(endpoint)));
    }

    /**
     * Do not let the program continue if this API is not available
     *
     * @param timeout threshold for what counts as not available
     */
    public void assertAvailable(int timeout) {
        if (!Api.pingURL(baseURL, timeout)) throw new ApiNotAvailableException(baseURL);
    }

    /**
     * Pings an HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
     * the 200-399 range.
     *
     * @param url     The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
     *                the total timeout is effectively two times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout, otherwise <code>false</code>.
     */
    public static boolean pingURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http"); // Otherwise, an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }
}
