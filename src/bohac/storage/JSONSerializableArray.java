package bohac.storage;

import org.json.JSONArray;

/**
 * Classes implementing this interface can be represented as a JSON Array
 */
public interface JSONSerializableArray {
    /**
     * JSON serializer method
     *
     * @return the serialized array in JSON
     */
    JSONArray toJSON();
}
