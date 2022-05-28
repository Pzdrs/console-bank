package bohac.storage;

import bohac.Configuration;
import bohac.util.Utils;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

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
