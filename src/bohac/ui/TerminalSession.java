package bohac.ui;

import bohac.Bank;
import bohac.Configuration;
import bohac.auditlog.AccountAuditLog;
import bohac.auditlog.AuditEvent;
import bohac.auditlog.events.AccessAuditEvent;
import bohac.entity.account.Account;
import bohac.entity.User;
import bohac.entity.account.Balance;
import bohac.transaction.Transaction;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static bohac.ui.TerminalUtils.*;

/**
 * The {@code TerminalSession} class represents a terminal session bound to a single logged-in user.
 */
public class TerminalSession implements Session {
    /**
     * The main {@code Scanner} instance for this program
     */
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final LanguageManager languageManager = LanguageManager.getInstance();
    private boolean active;

    private TerminalSession() {
        this.active = true;
    }

    /**
     * Static factory method. Could have used a constructor but why not switch things up.
     *
     * @return a new {@code TerminalSession} instance
     */
    public static TerminalSession createSession() {
        languageManager.setLocale(Configuration.DEFAULT_LANGUAGE);
        return new TerminalSession();
    }

    @Override
    public void onLogin(User user) {
        String userLocale = user.getPreferences().getProperty("locale");
        if (userLocale != null) languageManager.setLocale(new Locale(userLocale));

        // Dashboard menu
        new Menu(
                new Menu.MenuItem("menu_accounts", () -> {
                    // Accounts menu
                    new Menu(
                            new Menu.MenuItem("menu_select_account", () -> {
                                chooseOne(user.getAccounts(), Account::getDisplayName, account -> {
                                    account.logAccess(user);
                                    // Account menu
                                    new Menu(
                                            new Menu.MenuItem("menu_make_transaction", () -> handleMakePayment(account, user)),
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
                                            new Menu.MenuItem("menu_settings", () -> {
                                                // Account settings menu
                                                new Menu(
                                                        new Menu.MenuItem("menu_account_settings_change_name", () -> handleChangeAccountName(user, account)),
                                                        new Menu.MenuItem("menu_account_settings_add_owner", () -> handleAddOwner(user, account)),
                                                        Menu.BACK_ITEM
                                                ).prompt();
                                            }),
                                            new Menu.MenuItem("menu_close_account", () -> handleCloseAccount(account, user)),
                                            Menu.BACK_ITEM
                                    ).prompt(() -> accountMenuBeforeEach(account));
                                });
                            }),
                            new Menu.MenuItem("menu_open_new_account", () -> {
                                // TODO: 5/17/2022 create new account
                            }),
                            Menu.BACK_ITEM
                    ).prompt(() -> accountsMenuBeforeEach(user));
                }),
                new Menu.MenuItem("menu_settings", () -> {
                    // User preferences menu
                    // TODO: 5/18/2022 user preferences
                }),
                new Menu.MenuItem("menu_logout", () -> {
                }).exitMenuAfter(),
                new Menu.MenuItem("menu_logout_exit", this::endSession).exitMenuAfter()
        ).prompt(() -> dashboardMenuBeforeEach(user));

        onLogout();
    }

    @Override
    public void onLogout() {
        // Revert the language back to the system defaults
        languageManager.setLocale(Configuration.DEFAULT_LANGUAGE);
        clear();
        System.out.println(languageManager.getString(isActive() ? "user_logout" : "user_logout_exit"));
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void endSession() {
        this.active = false;
    }

    private Menu.MenuItem.Result handleAddOwner(User loggedInUser, Account account) {
        AtomicReference<String> result = new AtomicReference<>();
        User[] potentialUsers;
        do {
            potentialUsers = Bank.users.search(TerminalUtils.promptString(languageManager.getString("search")));
            if (potentialUsers.length == 0) System.out.println(languageManager.getString("error_user_not_found"));
            else {
                chooseOne(potentialUsers, null, user -> {
                    if (account.addOwner(loggedInUser, user, languageManager.getString("account_owner_added", "owner", user.getFullName()))) {
                        result.set(languageManager.getString("account_owner_added", "owner", user.getFullName()));
                    } else {
                        result.set(languageManager.getString("account_owner_already_in", "owner", user.getFullName()));
                    }
                });
            }
        } while (potentialUsers.length == 0);
        return new Menu.MenuItem.Result(false, result.get());
    }

    private void handleViewTransactionHistory(Account account, Comparator<Transaction> order, String orderLabel) {
        List<Transaction> transactionHistory = account.getTransactionHistory();
        System.out.println(languageManager.getString("account_showing_transactions",
                Map.of(
                        "count", transactionHistory.size(),
                        "order", orderLabel
                )));
        System.out.println();
        transactionHistory.sort(order);
        transactionHistory.forEach(System.out::println);
        Menu.BACK_ONLY.prompt();
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
        Menu.BACK_ONLY.prompt();
    }

    private Menu.MenuItem.Result handleChangeAccountName(User user, Account account) {
        String name = promptString("New account name");
        account.changeName(user, name, languageManager.getString("account_name_changed", "name", name));
        return new Menu.MenuItem.Result(false, languageManager.getString("account_name_changed", "name", name));
    }

    private Menu.MenuItem.Result handleCloseAccount(Account account, User user) {
        AtomicBoolean result = new AtomicBoolean(false);
        AtomicReference<String> resultMessage = new AtomicReference<>();
        new Menu(
                new Menu.MenuItem("menu_close_account_confirm", () -> {
                    boolean closureResult = account.close(user, languageManager.getString("account_closed"));
                    result.set(closureResult);
                    // Only add a message if the account couldn't be closed - success message wouldn't show up anyway, because
                    // we're travelling down the menu tree
                    resultMessage.set(closureResult ? null : languageManager.getString("account_close_failed"));
                }).exitMenuAfter(),
                Menu.BACK_ITEM
        ).prompt(() -> System.out.println(languageManager.getString("account_close_confirmation")));
        return new Menu.MenuItem.Result(result.get(), resultMessage.get());
    }

    private Menu.MenuItem.Result handleMakePayment(Account account, User user) {
        AtomicReference<String> message = new AtomicReference<>();
        Account[] potentialAccounts;
        do {
            potentialAccounts = Bank.accounts.search(TerminalUtils.promptString(languageManager.getString("search")));
            if (potentialAccounts.length == 0) System.out.println(languageManager.getString("error_account_not_found"));
            else {
                chooseOne(potentialAccounts, null, receiverAccount -> {
                    clear();
                    System.out.println(languageManager.getString("amount") + ":");
                    float amount = (float) promptNumericDouble("> ", languageManager);
                    clear();
                    System.out.println(languageManager.getString("account_transaction_confirmation", Map.of(
                            "amount", new Balance(account.getCurrency(), amount),
                            "account", receiverAccount
                    )));
                    new Menu(
                            new Menu.MenuItem("menu_authorize_transaction", () -> {
                                // Was the payment successful?
                                if (!account.authorizePayment(amount, receiverAccount, user)) {
                                    message.set(languageManager.getString("account_insufficient_funds"));
                                } else message.set(languageManager.getString("account_transaction_successful"));
                            }).exitMenuAfter(),
                            Menu.BACK_ITEM
                    ).dontClear().prompt();
                });
            }
        } while (potentialAccounts.length == 0);
        return new Menu.MenuItem.Result(false, message.get());
    }

    private void accountsMenuBeforeEach(User user) {
        Account[] accounts = user.getAccounts();
        System.out.println(
                center(user.getAccountsOverview(), printHeaderAndGetWidth(languageManager.getString("menu_header_accounts")))
        );
        for (int i = 0; i < accounts.length; i++) {
            System.out.printf("[%d] %s\n", i + 1, accounts[i].getDisplayName());
        }
    }

    private void accountMenuBeforeEach(Account account) {
        String balanceAndOwnerCount = center(
                String.format("%s: %s | %s: %d",
                        languageManager.getString("balance"),
                        account.getBalance(),
                        languageManager.getString("owners"),
                        account.getOwners().size()),
                printHeaderAndGetWidth(account.getName(false)));
        System.out.println(balanceAndOwnerCount);
        AccessAuditEvent lastAccess = account.getAuditLog().getLastAccess();
        System.out.println(center(String
                        .format(languageManager.getString("last_access")
                                + ": %s", lastAccess == null ? languageManager.getString("account_last_access_empty") : lastAccess.toStringShort()),
                balanceAndOwnerCount)
        );
    }

    private void dashboardMenuBeforeEach(User user) {
        System.out.println(center(
                languageManager.getString("user_welcome", "user", user.getFullName()),
                printHeaderAndGetWidth(languageManager.getString("menu_header_dashboard"))
        ));
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
}
