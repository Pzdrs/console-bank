package bohac.storage;

import bohac.util.Utils;
import bohac.entity.User;
import org.json.JSONObject;

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

    /**
     * Advanced search method - needs some tweaking as time goes by
     *
     * @param s searched value
     * @return array of possible numbers
     */
    public User[] search(String s) {
        // exact id
        try {
            Optional<User> byID = getByID(UUID.fromString(s));
            if (byID.isPresent()) return new User[]{byID.get()};
        } catch (IllegalArgumentException ignored) {
            // invalid uuid
        }

        // exact login
        Optional<User> byLogin = getByLogin(s);
        if (byLogin.isPresent()) return new User[]{byLogin.get()};

        // similar name + last name
        List<User> potentialUsers = new ArrayList<>();
        for (User user : users) {
            if (Utils.similarity(user.getFullName(), s) > 0.4) potentialUsers.add(user);
        }

        return potentialUsers.toArray(User[]::new);
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
