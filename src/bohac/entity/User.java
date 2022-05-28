package bohac.entity;

import bohac.Bank;
import bohac.Configuration;
import bohac.entity.account.Account;
import bohac.storage.JSONSerializable;
import bohac.storage.UserPreferences;
import bohac.util.Utils;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * This objects represents a single user
 */
public final class User implements JSONSerializable {
    /**
     * What name is used for the data folder for this entity
     */
    public static final String DATA_FOLDER = "users";
    /**
     * When the program starts for the first time, no user data is available yet, so it creates these contacts to get you going.
     */
    public static final List<User> DEFAULT_USERS = List.of(
            new User("admin", "admin@bank.com", "The", "Administrator", "admin", true)
    );
    private final UUID id;
    private final String email;
    private final String username;
    private final String name, lastName, password;
    private final LocalDateTime created;
    private final UserPreferences preferences;
    private final boolean admin;

    /**
     * This constructor is used, when loading data from the disk
     */
    public User(UUID id, String username, String name, String lastName, String email, String password, LocalDateTime created, JSONObject preferences, boolean admin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.created = created;
        this.preferences = preferences.has("preferences") ? UserPreferences.load(preferences.getJSONObject("preferences")) : new UserPreferences();
        this.admin = admin;
    }

    /**
     * This constructor is used when creating a new account programmatically
     */
    public User(String username, String email, String name, String lastName, String password, boolean admin) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = encryptPassword(password);
        this.created = LocalDateTime.now();
        this.preferences = new UserPreferences();
        this.admin = admin;
    }

    /**
     * @return all registered accounts that have this user in the owner list and are not closed
     */
    public Account[] getAccounts() {
        return Bank.accounts.getUserAccounts(this);
    }

    /**
     * Encrypts a passed in raw text password
     *
     * @param password raw text password
     * @return encrypted password
     */
    public static String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    /**
     * @param password raw text password
     * @return whether the passed in raw password matches this users actual password
     */
    public boolean isPasswordValid(String password) {
        return new BCryptPasswordEncoder().matches(password, this.password);
    }

    /**
     * @return the user's full name
     */
    public String getFullName() {
        return String.format("%s %s", name, lastName);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public boolean isAdmin() {
        return admin;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    @Override
    public String toString() {
        return getFullName() + " - " + email;
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("username", username)
                .put("password", password)
                .put("email", email)
                .put("name", name)
                .put("last_name", lastName)
                .put("admin", admin)
                .put("preferences", preferences.toJSON())
                .put("created_at", created.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(created)));
    }

    public void save() {
        save(DATA_FOLDER, id);
    }

    public static User load(JSONObject object) {
        Utils.printDebugMessage(String.format("Loading user %s", object.getString("id")));
        return new User(UUID.fromString(object.getString("id")),
                object.getString("username"),
                object.getString("name"),
                object.getString("last_name"),
                object.getString("email"),
                object.getString("password"),
                Utils.parseEpoch(object.getLong("created_at")),
                object,
                object.getBoolean("admin"));
    }
}
