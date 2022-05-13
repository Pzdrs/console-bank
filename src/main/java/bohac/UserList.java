package bohac;

import bohac.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class UserList extends ArrayList<User> {
    public UserList() {
    }

    public UserList(Collection<? extends User> c) {
        super(c);
    }

    public User getById(UUID id) {
        return stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
    }
}
