package bohac.entity;

import bohac.Bank;
import bohac.Configuration;
import bohac.entity.account.Account;
import bohac.storage.JSONSerializable;
import bohac.ui.TerminalSession;
import bohac.util.Utils;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class User implements JSONSerializable {
    public static final String FILE_NAME = "users.json";
    /**
     * When the program starts for the first time, no user data is available yet, so it creates these contacts to get you going.
     */
    public static final List<User> DEFAULT_USERS = List.of(
            new User("admin", "admin@bank.com", "The", "Administrator", "admin")
    );
    private final UUID id;
    private final String email;
    private String username;
    private String name, lastName, password;
    private final LocalDateTime created;
    private UserPreferences preferences;

    public User(UUID id, String username, String name, String lastName, String email, String password, LocalDateTime created, JSONObject preferences) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.created = created;
        this.preferences = preferences.has("preferences") ? UserPreferences.load(preferences.getJSONObject("preferences")) : new UserPreferences();
    }

    public User(UUID id, String email, String username, String name, String lastName, String password, LocalDateTime created, UserPreferences preferences) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.created = created;
        this.preferences = preferences;
    }

    private User(String username, String email, String name, String lastName, String password) {
        this(UUID.randomUUID(), username, name, lastName, email, encryptPassword(password), LocalDateTime.now(), new UserPreferences());
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

    public String getFullName() {
        return String.format("%s %s", name, lastName);
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public Account[] getAccounts() {
        return Bank.accounts.get().stream().filter(account -> account.getOwners().contains(this)).sorted().toArray(Account[]::new);
    }

    public String getAccountsOverview() {
        int accounts = getAccounts().length;
        return TerminalSession.languageManager.getString("menu_accounts_overview", Map.of(
                "accounts", accounts,
                "slotsLeft", Configuration.USER_MAX_ACCOUNTS - accounts
        )) + "\n";
    }

    public static String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public boolean isPasswordValid(String password) {
        return new BCryptPasswordEncoder().matches(password, this.password);
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
                .put("preferences", preferences.toJSON())
                .put("created_at", created.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(created)));
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
                object);
    }
}
