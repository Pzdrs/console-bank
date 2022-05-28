package bohac;

import bohac.entity.User;
import bohac.entity.account.Account;
import bohac.storage.AccountList;
import bohac.storage.UserList;
import bohac.ui.TerminalSession;
import bohac.util.API;
import bohac.util.Utils;

import java.nio.file.Paths;
import java.util.*;

/**
 * The main class that initially puts the frontend and backend together. Stores most of the data that is needed for the
 * entirety of the program's lifespan.
 */
public class Bank {
    public static final API API = new API("https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest");
    /**
     * All users loaded from the disk
     */
    public static UserList users;
    /**
     * All accounts loaded from the disk
     */
    public static AccountList accounts;
    /**
     * This {@code Map<UUID, Long>} keeps track of the timed out user accounts
     */
    private static final Map<UUID, Long> USER_LOGIN_TIMEOUT = new HashMap<>();

    /**
     * The main entrypoint of the program
     *
     * @param args any arguments passed in from the command line
     */
    public static void main(String[] args) {
        // Creating an instance of TerminalSession
        TerminalSession session = TerminalSession.createSession();

        // Loading in all the necessary data to memory
        users = UserList.load(Paths.get(Configuration.DATA_ROOT, User.DATA_FOLDER).toFile());

        accounts = AccountList.load(Paths.get(Configuration.DATA_ROOT, Account.DATA_FOLDER).toFile());

        // Authentication workflow
        int tries = 0;
        String lastLogin = "";
        while (session.isActive()) {
            String login = session.promptUsername();

            if (!lastLogin.equals(login)) tries = 0;
            lastLogin = login;

            Optional<User> potentialUser = users.getByLogin(login);
            if (potentialUser.isPresent()) {
                User user = potentialUser.get();

                // Check if this user is on timeout
                if (USER_LOGIN_TIMEOUT.containsKey(user.getId())) {
                    if (USER_LOGIN_TIMEOUT.get(user.getId()) <= System.currentTimeMillis()) {
                        USER_LOGIN_TIMEOUT.remove(user.getId());
                    } else {
                        session.stillOnTimeout(USER_LOGIN_TIMEOUT.get(user.getId()) - System.currentTimeMillis());
                        continue;
                    }
                }

                String password = session.promptPassword();
                if (user.isPasswordValid(password)) {
                    session.onLogin(user);
                } else {
                    // Wrong password
                    session.wrongPassword(Configuration.MAX_LOGIN_TRIES - ++tries);
                    if (tries == Configuration.MAX_LOGIN_TRIES) {
                        USER_LOGIN_TIMEOUT.put(user.getId(), System.currentTimeMillis() + Configuration.LOGIN_TIMEOUT_DURATION.toMillis());
                        tries = 0;
                    }
                }
            } else {
                session.userNotFound(login);
            }
        }
    }
}
