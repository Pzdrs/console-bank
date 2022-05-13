package bohac;

import bohac.entity.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
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

    public static UserList load(String path) {
        UserList users = new UserList();
        File userFile = new File(path);
        if (!userFile.exists()) {
            createDefaultFile(path);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            array.forEach(user -> users.add(User.load((JSONObject) user)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    private static void createDefaultFile(String path) {
        JSONArray defaultUsers = new JSONArray();
        User.DEFAULT_USERS.forEach(user -> defaultUsers.put(user.toJSON()));
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(defaultUsers.toString());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create the default users.json file");
        }
    }

    @Override
    public Iterator<User> iterator() {
        return users.iterator();
    }
}
