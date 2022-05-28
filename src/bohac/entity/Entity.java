package bohac.entity;

import bohac.Configuration;
import bohac.storage.JSONSerializable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Represents an entity, which is also serializable to JSON and will be saved to the disk during the program lifecycle
 */
public interface Entity extends JSONSerializable {
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
