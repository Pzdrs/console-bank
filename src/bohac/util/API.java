package bohac.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Represents a single REST API
 */
public class API {
    private final String baseURL;

    public API(String baseURL) {
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
}
