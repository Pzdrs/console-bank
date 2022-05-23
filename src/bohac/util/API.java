package bohac.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {
    private final String baseURL;

    public API(String baseURL) {
        this.baseURL = baseURL;
    }

    public InputStream get(String endpoint) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseURL + endpoint).openConnection();

            connection.setRequestProperty("accept", "application/json");

            return connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getJSON(String endpoint) {
        return new JSONObject(new JSONTokener(get(endpoint)));
    }
}
