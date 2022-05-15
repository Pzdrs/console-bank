package bohac;

import bohac.entity.Account;
import bohac.entity.User;
import bohac.entity.UserPreferences;
import bohac.storage.AccountList;
import bohac.storage.UserList;
import bohac.ui.TerminalSession;

import java.nio.file.Paths;
import java.util.*;

public class Bank {
    public static UserList users;
    public static AccountList accounts;

    private static final Map<UUID, Long> USER_LOGIN_TIMEOUT = new HashMap<>();

    public static void main(String[] args) {
        TerminalSession session = TerminalSession.createSession();

        users = UserList.load(Paths.get(Configuration.DATA_ROOT, User.FILE_NAME));

        accounts = AccountList.load(Paths.get(Configuration.DATA_ROOT, Account.FILE_NAME));
        accounts.initializeTransactions();


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
