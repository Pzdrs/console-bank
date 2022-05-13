package bohac.entity;

import bohac.JSONSerializable;
import bohac.Utils;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class User implements JSONSerializable {
    /**
     * When the program starts for the first time, no user data is available yet, so it creates these contacts to get you going.
     */
    public static final List<User> DEFAULT_USERS = List.of(
            new User("admin", "admin@bank.com", "The", "Administrator", "admin")
    );
    private UUID id;
    private String username, email;
    private String name, lastName, password;
    private LocalDateTime created;

    public User(UUID id, String username, String name, String lastName, String email, String password, LocalDateTime created) {
        this.id = id;
        this.username = username;
        this.password = encryptPassword(password);
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.created = created;
    }

    private User(String username, String email, String name, String lastName, String password) {
        this(UUID.randomUUID(), username, name, lastName, email, password, LocalDateTime.now());
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


    public static String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public boolean isPasswordValid(String password) {
        return new BCryptPasswordEncoder().matches(password, this.password);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", created=" + created +
                '}';
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("username", username)
                .put("password", username)
                .put("email", email)
                .put("name", name)
                .put("last_name", lastName)
                .put("created_at", created.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(created)));
    }

    public static User load(JSONObject object) {
        System.out.println(Utils.getMessage("debug_user_load").replace("{user}", object.getString("username")));
        return new User(UUID.fromString(object.getString("id")),
                object.getString("username"),
                object.getString("name"),
                object.getString("last_name"),
                object.getString("email"),
                object.getString("password"),
                LocalDateTime.ofInstant(Instant.ofEpochSecond(object.getLong("created_at")), ZoneId.systemDefault()));
    }
}
