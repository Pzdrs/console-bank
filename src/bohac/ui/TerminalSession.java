package bohac.ui;

import bohac.Configuration;
import bohac.entity.account.Account;
import bohac.entity.User;

import java.time.Duration;
import java.util.Locale;
import java.util.Scanner;

import static bohac.ui.TerminalUtils.*;

public class TerminalSession {
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final LanguageManager languageManager = LanguageManager.getInstance();
    private boolean active;

    private TerminalSession() {
        this.active = true;
    }

    public static TerminalSession createSession() {
        languageManager.setLocale(Configuration.DEFAULT_LANGUAGE);
        return new TerminalSession();
    }

    public void endSession() {
        this.active = false;
    }

    public void onLogin(User user) {
        String userLocale = user.getPreferences().getProperty("locale");
        if (userLocale != null) languageManager.setLocale(new Locale(userLocale));

        new Menu(
                new Menu.MenuItem("menu_accounts", () -> {
                    new Menu(
                            new Menu.MenuItem("menu_select_account", () -> {
                                // Make the user choose an account
                                Account account = (Account) chooseOne(user.getAccounts(), o -> ((Account) o).getDisplayName());
                                new Menu(
                                        new Menu.MenuItem("menu_make_transaction", () -> {
                                        }),
                                        new Menu.MenuItem("menu_open_audit_log", () -> {
                                        }),
                                        new Menu.MenuItem("menu_close_account", () -> {
                                        }),
                                        new Menu.MenuItem("menu_back", () -> false)
                                ).prompt(() -> account.showOverview(languageManager));
                            }),
                            new Menu.MenuItem("menu_open_new_account", () -> {
                                // create new account
                            }),
                            new Menu.MenuItem("menu_back", () -> false)
                    ).prompt(() -> {
                        Account[] accounts = user.getAccounts();
                        System.out.println(center(
                                String.format("You own/co-own %d %s (%d slots left)\n",
                                        accounts.length,
                                        accounts.length == 1 ? "account" : "accounts",
                                        Configuration.USER_MAX_ACCOUNTS - accounts.length),
                                printHeaderAndGetWidth(languageManager.getString("menu_header_accounts"))
                        ));
                        for (int i = 0; i < accounts.length; i++) {
                            System.out.printf("[%d] %s\n", i + 1, accounts[i].getDisplayName());
                        }
                    });
                }),
                new Menu.MenuItem("menu_my_preferences", () -> {
                }),
                new Menu.MenuItem("menu_logout", () -> false),
                new Menu.MenuItem("menu_logout_exit", () -> {
                    endSession();
                    return false;
                })
        ).prompt(() -> System.out.println(center(
                        languageManager.getString("user_welcome", "user", user.getFullName()),
                        printHeaderAndGetWidth(languageManager.getString("menu_header_dashboard"))
                ))
        );
        logout();
    }

    private void logout() {
        // Revert the language back to the system defaults
        languageManager.setLocale(Configuration.DEFAULT_LANGUAGE);
        clear();
        System.out.println(languageManager.getString(isActive() ? "user_logout" : "user_logout_exit"));
    }

    public String promptUsername() {
        System.out.printf("%s: ", languageManager.getString("login_prompt_username"));
        return SCANNER.nextLine().trim().split(" ")[0];
    }

    public String promptPassword() {
        System.out.printf("%s: ", languageManager.getString("login_prompt_password"));
        return SCANNER.nextLine().trim().split(" ")[0];
    }

    public void userNotFound(String login) {
        System.out.printf("%s\n", languageManager.getString("login_user_not_found", "user", login));
    }

    public void wrongPassword(int triesLeft) {
        System.out.printf("%s\n", languageManager.getString(triesLeft == 0 ? "login_wrong_password_timeout" : "login_wrong_password", "tries", String.valueOf(triesLeft)));
    }

    public void stillOnTimeout(long timeLeft) {
        long secondsLeft = Duration.ofMillis(timeLeft).toSeconds();
        System.out.printf("%s\n", languageManager.getString("login_still_timeout")
                .replace("{duration}", String.format("%dh%02dm%02ds", secondsLeft / 3600, (secondsLeft % 3600) / 60, (secondsLeft % 60))));
    }

    public boolean isActive() {
        return active;
    }
}
