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

    /**
     * Saver method for any entity implementing this interface
     *
     * @param dataFolder the folder that contains all file representations of this entity
     * @param id         entity id - file name
     */
    default void save(String dataFolder, UUID id) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(Configuration.DATA_ROOT, dataFolder, id + ".json").toFile()))) {
            writer.write(toJSON().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
