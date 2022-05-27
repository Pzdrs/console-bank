package bohac.ui;

import bohac.Bank;
import bohac.Configuration;
import bohac.auditlog.AccountAuditLog;
import bohac.auditlog.AuditEvent;
import bohac.auditlog.events.AccessAuditEvent;
import bohac.entity.User;
import bohac.entity.account.Account;
import bohac.entity.account.Balance;
import bohac.transaction.Transaction;
import bohac.util.Utils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
        if (userLocale != null) {
            languageManager.setLocale(Utils.tagToLocale(userLocale));
        }

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
                            new Menu.MenuItem("menu_open_new_account", () -> handleOpenAccount(user)),
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

    /**
     * Open account handler
     *
     * @param user user
     */
    private Menu.MenuItem.Result handleOpenAccount(User user) {
        // If the user is over the limit
        if (user.getAccounts().length >= Configuration.USER_MAX_ACCOUNTS)
            return new Menu.MenuItem.Result(false, languageManager.getString("account_open_limit"));
        // Choose account type
        AtomicReference<Account> accountAtomic = new AtomicReference<>();
        chooseOne(Account.Type.values(), null, type -> {
            clear();
            // Choosing the type of currency
            Currency currency;
            do {
                String userLocale = user.getPreferences().getProperty("locale");
                Currency proposed = LanguageManager.getCurrency(userLocale == null ? Configuration.DEFAULT_LANGUAGE : Utils.tagToLocale(userLocale));
                if (proposed != null) {
                    String input = promptString(languageManager.getString("account_open_currency_default", "currency", proposed.getDisplayName()));
                    currency = input.isEmpty() ? proposed : LanguageManager.getCurrency(input.toUpperCase());
                } else {
                    currency = LanguageManager.getCurrency(promptString(languageManager.getString("account_open_currency")).toUpperCase());
                }
                if (currency == null) System.out.println(languageManager.getString("invalid_currency"));
            } while (currency == null);
            clear();
            // Coming up with the account name
            String defaultAccountName = Utils.getDefaultAccountName(type, user);
            String name = promptString(languageManager.getString("account_open_name", "default", defaultAccountName));
            if (name.isEmpty()) name = defaultAccountName;
            Account account = new Account(type, currency, user, name);
            Bank.accounts.add(account);
            accountAtomic.set(account);
        });
        // If the user cancelled at the type choice menu, exit to last menu and say nothing
        if (accountAtomic.get() == null) return new Menu.MenuItem.Result(false, null);
        Account account = accountAtomic.get();
        return new Menu.MenuItem.Result(false, languageManager.getString("account_opened", Map.of(
                "type", account.getType(),
                "name", account.getName()
        )));
    }

    /**
     * Account user addition handler
     *
     * @param loggedInUser logged-in user
     * @param account      account
     * @return menu item {@code Result}
     */
    private Menu.MenuItem.Result handleAddOwner(User loggedInUser, Account account) {
        AtomicReference<String> result = new AtomicReference<>();
        User[] potentialUsers;
        do {
            potentialUsers = Bank.users.search(promptString(languageManager.getString("search")));
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

    /**
     * View transaction history handler
     *
     * @param account    account
     * @param order      order of transactions
     * @param orderLabel order label
     */
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

    /**
     * View audit log handler
     *
     * @param auditLog   account audit log
     * @param order      audit event order
     * @param orderLabel order label
     */
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

    /**
     * Account name change handler
     *
     * @param user    logged-in user
     * @param account account
     * @return menu item {@code Result}
     */
    private Menu.MenuItem.Result handleChangeAccountName(User user, Account account) {
        String name = promptString("New account name");
        account.changeName(user, name, languageManager.getString("account_name_changed", "name", name));
        return new Menu.MenuItem.Result(false, languageManager.getString("account_name_changed", "name", name));
    }

    /**
     * Account closure handler
     *
     * @param account account
     * @param user    logged-in user
     * @return menu item {@code Result}
     */
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

    /**
     * Payment handler
     *
     * @param account account
     * @param user    logged-in user
     * @return menu item {@code Result}
     */
    private Menu.MenuItem.Result handleMakePayment(Account account, User user) {
        AtomicReference<String> message = new AtomicReference<>();
        Account[] potentialAccounts;
        do {
            potentialAccounts = Bank.accounts.search(promptString(languageManager.getString("search")));
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

    /**
     * Print out all available accounts and a header each accounts menu iteration
     *
     * @param user logged-in user
     */
    private void accountsMenuBeforeEach(User user) {
        Account[] accounts = Arrays.stream(user.getAccounts()).sorted().toArray(Account[]::new);
        System.out.println(
                center(TerminalUtils.getAccountsOverview(accounts), printHeaderAndGetWidth(languageManager.getString("menu_header_accounts")))
        );
        for (int i = 0; i < accounts.length; i++) {
            System.out.printf("[%d] %s\n", i + 1, accounts[i].getDisplayName());
        }
    }

    /**
     * Print out account overview and a header each account menu iteration
     *
     * @param account account
     */
    private void accountMenuBeforeEach(Account account) {
        String balanceAndOwnerCount = center(
                String.format("%s | %s: %s | %s: %d",
                        account.getType().shortName(),
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

    /**
     * Print out header and a welcome message each dashboard menu iteration
     *
     * @param user logged-in user
     */
    private void dashboardMenuBeforeEach(User user) {
        System.out.println(center(
                languageManager.getString("user_welcome", "user", user.getFullName()),
                printHeaderAndGetWidth(languageManager.getString("menu_header_dashboard"))
        ));
    }

    /**
     * Prompt the user for a username
     *
     * @return normalized user login
     */
    public String promptUsername() {
        return promptString(languageManager.getString("login_prompt_username")).trim().split(" ")[0];
    }

    /**
     * Prompt the user for a password
     *
     * @return normalized user password
     */
    public String promptPassword() {
        return promptString(languageManager.getString("login_prompt_password")).trim().split(" ")[0];
    }

    /**
     * User not found error
     *
     * @param login user login that hasn't been found
     */
    public void userNotFound(String login) {
        System.out.printf("%s\n", languageManager.getString("login_user_not_found", "user", login));
    }

    /**
     * Wrong password error
     *
     * @param triesLeft tries left before timeout
     */
    public void wrongPassword(int triesLeft) {
        System.out.printf("%s\n", languageManager.getString(triesLeft == 0 ? "login_wrong_password_timeout" : "login_wrong_password", "tries", String.valueOf(triesLeft)));
    }

    /**
     * If a user tries to log in but is still on timeout, this method is called
     *
     * @param timeLeft time left before begin able to log in again
     */
    public void stillOnTimeout(long timeLeft) {
        long secondsLeft = Duration.ofMillis(timeLeft).toSeconds();
        System.out.printf("%s\n", languageManager.getString("login_still_timeout")
                .replace("{duration}", String.format("%dh%02dm%02ds", secondsLeft / 3600, (secondsLeft % 3600) / 60, (secondsLeft % 60))));
    }
}
