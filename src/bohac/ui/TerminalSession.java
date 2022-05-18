package bohac.ui;

import bohac.Bank;
import bohac.Configuration;
import bohac.auditlog.AccountAuditLog;
import bohac.auditlog.AuditEvent;
import bohac.entity.account.Account;
import bohac.entity.User;
import bohac.transaction.Transaction;

import java.time.Duration;
import java.util.*;

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

        // Dashboard menu
        new Menu(
                new Menu.MenuItem("menu_accounts",
                        // Accounts menu
                        new Menu(
                                new Menu.MenuItem("menu_select_account", () -> {
                                    chooseOne(user.getAccounts(), Account::getDisplayName, account -> {
                                        new Menu(
                                                new Menu.MenuItem("menu_make_transaction", () -> {
                                                    // TODO: 5/17/2022 make transactions
                                                }),
                                                new Menu.MenuItem("menu_view_transaction_history", () -> handleViewTransactionHistory(
                                                        account,
                                                        Transaction.CHRONOLOGICAL,
                                                        languageManager.getString("order1"))
                                                ),
                                                new Menu.MenuItem("menu_view_transaction_history2", () -> handleViewTransactionHistory(
                                                        account,
                                                        Transaction.AMOUNT,
                                                        languageManager.getString("order2"))
                                                ),
                                                new Menu.MenuItem("menu_open_audit_log", () -> handleViewAuditLog(
                                                        account.getAuditLog(),
                                                        AuditEvent.CHRONOLOGICAL,
                                                        languageManager.getString("order1"))
                                                ),
                                                new Menu.MenuItem("menu_settings",
                                                        // Account settings menu
                                                        new Menu(
                                                                new Menu.MenuItem("menu_account_settings_change_name", () -> {
                                                                    // TODO: 5/17/2022 change name
                                                                }),
                                                                new Menu.MenuItem("menu_account_settings_add_owner", () -> handleAddOwner(account)),
                                                                Menu.BACK_ITEM
                                                        )),
                                                new Menu.MenuItem("menu_close_account", () -> {
                                                    // TODO: 5/17/2022 close account
                                                }),
                                                Menu.BACK_ITEM
                                        ).prompt(() -> account.showOverview(languageManager));
                                    });
                                }),
                                new Menu.MenuItem("menu_open_new_account", () -> {
                                    // TODO: 5/17/2022 create new account
                                }),
                                Menu.BACK_ITEM
                        ), () -> {
                    Account[] accounts = user.getAccounts();
                    System.out.println(
                            center(user.getAccountsOverview(), printHeaderAndGetWidth(languageManager.getString("menu_header_accounts")))
                    );
                    for (int i = 0; i < accounts.length; i++) {
                        System.out.printf("[%d] %s\n", i + 1, accounts[i].getDisplayName());
                    }
                }),
                new Menu.MenuItem("menu_settings",
                        // User preferences menu
                        new Menu()),
                new Menu.MenuItem("menu_logout", () -> false),
                new Menu.MenuItem("menu_logout_exit", () -> {
                    endSession();
                    return false;
                })
        ).prompt(() -> System.out.println(center(
                languageManager.getString("user_welcome", "user", user.getFullName()),
                printHeaderAndGetWidth(languageManager.getString("menu_header_dashboard"))
        )));
        logout();
    }

    private void logout() {
        // Revert the language back to the system defaults
        languageManager.setLocale(Configuration.DEFAULT_LANGUAGE);
        clear();
        System.out.println(languageManager.getString(isActive() ? "user_logout" : "user_logout_exit"));
    }

    private void handleAddOwner(Account account) {
        new Menu(
                new Menu.MenuItem("menu_account_settings_add_more_owners", (() -> {
                })),
                Menu.BACK_ITEM
        ).prompt(() -> {
            User[] potentialUsers;
            do {
                potentialUsers = Bank.users.search(TerminalUtils.promptString(languageManager.getString("search")));
                if (potentialUsers.length == 0) System.out.println(languageManager.getString("error_user_not_found"));
                else {
                    chooseOne(potentialUsers, null, user -> {
                        clear();
                        System.out.println(languageManager.getString("account_owner_added", "owner", user.getFullName()));
                        System.out.println();
                        account.addOwner(user);
                    });
                }
            } while (potentialUsers.length == 0);
        });
    }

    private void handleViewTransactionHistory(Account account, Comparator<Transaction> order, String orderLabel) {
        List<Transaction> transactionHistory = new ArrayList<>(account.getTransactionHistory());
        System.out.println(languageManager.getString("account_showing_transactions",
                Map.of(
                        "count", transactionHistory.size(),
                        "order", orderLabel
                )));
        System.out.println();
        transactionHistory.sort(order);
        transactionHistory.forEach(System.out::println);
        Menu.BACK_ONLY.prompt(false);
    }

    private void handleViewAuditLog(AccountAuditLog auditLog, Comparator<AuditEvent> order, String orderLabel) {
        List<AuditEvent> events = new ArrayList<>(auditLog.eventList());
        System.out.println(languageManager.getString("account_showing_audit_log",
                Map.of(
                        "count", auditLog.eventList().size(),
                        "order", orderLabel
                )));
        System.out.println();
        events.sort(order);
        events.forEach(System.out::println);
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
