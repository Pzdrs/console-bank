package bohac.storage;

import bohac.Bank;
import bohac.Configuration;
import bohac.util.Utils;
import bohac.entity.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The {@code UserList} represents a collection of {@code User} objects
 */
public class UserList implements Iterable<User>, JSONSerializableArray {
    private final List<User> users;

    private UserList() {
        this.users = new ArrayList<>();
    }

    /**
     * Add a user to the collection
     *
     * @param user user
     */
    public void add(User user) {
        users.add(user);
    }

    /**
     * Get a user by their ID
     *
     * @param uuid id
     * @return potentially empty {@code Optional<User>} object
     */
    public Optional<User> getByID(UUID uuid) {
        return users.stream().filter(user -> user.getId().equals(uuid)).findFirst();
    }

    /**
     * Get a user by their login - username or email
     *
     * @param login user login
     * @return potentially empty {@code Optional<User>} object
     */
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
        if (s.isEmpty()) return new User[1];
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

    /**
     * Loads all the data from the data folder to memory. Acts as a static factory method also.
     *
     * @param dataFolder dataFolder
     * @return new instance of the {@code UserList} object with the loaded data
     */
    public static UserList load(File dataFolder) {
        UserList users = new UserList();
        // Make sure the data folder exists
        if (!dataFolder.exists()) {
            Utils.printDebugMessage("User data folder not found, creating..." + (dataFolder.mkdir() ? "done" : "error"));
        }
        File[] userFiles = dataFolder.listFiles();
        if (userFiles != null) {
            if (userFiles.length == 0) {
                User.DEFAULT_USERS.forEach(user -> {
                    Utils.printDebugMessage("Creating a default user: " + user);
                    users.add(user);
                    user.save();
                });
            }
            ;
            for (File userFile : userFiles) {
                Utils.loadFile(userFile, object -> users.add(User.load(object)));
            }
        }
        return users;
    }

    @Override
    public JSONArray toJSON() {
        JSONArray array = new JSONArray();
        users.forEach(user -> array.put(user.toJSON()));
        return array;
    }

    @Override
    public Iterator<User> iterator() {
        return users.iterator();
    }
}
