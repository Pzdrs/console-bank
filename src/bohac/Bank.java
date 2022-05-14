package bohac;

import bohac.entity.Account;
import bohac.entity.User;
import bohac.storage.AccountList;
import bohac.storage.UserList;
import bohac.ui.TerminalSession;

import java.nio.file.Paths;
import java.util.*;

public class Bank {
    static Scanner scanner = new Scanner(System.in);
    public static UserList users;
    public static AccountList accounts;

    private static Map<UUID, Long> userLoginTimeout = new HashMap<>();

    public static void main(String[] args) {
        TerminalSession session = TerminalSession.createSession();

        users = UserList.load(Paths.get(Configuration.DATA_ROOT.toString(), User.FILE_NAME));

        accounts = AccountList.load(Paths.get(Configuration.DATA_ROOT.toString(), Account.FILE_NAME));
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
                if (userLoginTimeout.containsKey(user.getId())) {
                    if (userLoginTimeout.get(user.getId()) <= System.currentTimeMillis()) {
                        userLoginTimeout.remove(user.getId());
                    } else {
                        session.stillOnTimeout(userLoginTimeout.get(user.getId()) - System.currentTimeMillis());
                        continue;
                    }
                }

                String password = session.promptPassword();
                if (user.isPasswordValid(password)) {
                    System.out.println("yes");
                } else {
                    // Wrong password
                    session.wrongPassword(Configuration.MAX_LOGIN_TRIES - ++tries);
                    if (tries == Configuration.MAX_LOGIN_TRIES) {
                        userLoginTimeout.put(user.getId(), System.currentTimeMillis() + Configuration.LOGIN_TIMEOUT_DURATION.toMillis());
                        tries = 0;
                    }
                }
            } else {
                session.userNotFound(login);
            }
        }
    }
}
