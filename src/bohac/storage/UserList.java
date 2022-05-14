package bohac.storage;

import bohac.Utils;
import bohac.entity.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class UserList implements Iterable<User> {
    private List<User> users;

    public UserList() {
        this.users = new ArrayList<>();
    }

    public void add(User user) {
        users.add(user);
    }

    public Optional<User> getByID(UUID uuid) {
        return users.stream().filter(user -> user.getId().equals(uuid)).findFirst();
    }

    public Optional<User> getByLogin(String login) {
        return users.stream().filter(user -> user.getEmail().equals(login) || user.getUsername().equals(login)).findFirst();
    }

    public static UserList load(Path path) {
        UserList users = new UserList();
        Utils.loadFile(path.toFile(), objects -> objects.forEach(user -> users.add(User.load((JSONObject) user))), defaultUsers -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
                writer.write(defaultUsers.toString());
            } catch (IOException e) {
                throw new RuntimeException(String.format("Couldn't create the default %s file", User.FILE_NAME));
            }
        }, User.DEFAULT_USERS);
        return users;
    }

    @Override
    public Iterator<User> iterator() {
        return users.iterator();
    }
}
