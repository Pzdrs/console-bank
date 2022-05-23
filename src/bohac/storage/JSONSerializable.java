package bohac.storage;

import org.json.JSONObject;

public interface JSONSerializable {
    /**
     * JSON serializer method
     *
     * @return the serialized object in JSON
     */
    JSONObject toJSON();
}
