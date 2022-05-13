package bohac.entity;

import bohac.UserPreferences;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID id;
    private UserPreferences preferences;
    private final String email;
    private String name, lastName, password;
    private final LocalDateTime created;

    public User(UUID id, String name, String lastName, String email, String password, LocalDateTime created) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.created = created;
    }

    public UUID getId() {
        return id;
    }

    public UserPreferences getPreferences() {
        return preferences;
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

    public boolean setName(String name) {
        this.name = name;
        return false;
    }

    public boolean setLastName(String lastName) {
        this.lastName = lastName;
        return false;
    }

    public boolean setPassword(String password) {
        try {
            this.password = new BCryptPasswordEncoder().encode(password);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", created=" + created +
                '}';
    }
}
