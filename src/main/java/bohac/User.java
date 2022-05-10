package bohac;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String name, lastName, email, password;
    private final LocalDateTime created;

    public User(UUID id, String name, String lastName, String email, String password, LocalDateTime created) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.created = created;
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
