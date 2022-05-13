package bohac;

import bohac.entity.User;
import bohac.ui.TerminalSession;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Bank {
    static Scanner scanner = new Scanner(System.in);
    public static UserList users;
    public static AccountList accounts;

    private static Map<UUID, Long> userLoginTimeout = new HashMap<>();

    public static void main(String[] args) {
        TerminalSession session = TerminalSession.createSession();

        users = UserList.load("data/users.json");

        accounts = AccountList.load("data/accounts.json");
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
