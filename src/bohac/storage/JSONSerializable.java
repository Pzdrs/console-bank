package bohac.storage;

import org.json.JSONObject;

/**
 * Classes implementing this interface can be represented as a JSON object
 */
public interface JSONSerializable {
    /**
     * JSON serializer method
     *
     * @return the serialized object in JSON
     */
    JSONObject toJSON();
}
