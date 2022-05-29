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

import java.text.Collator;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static bohac.ui.TerminalUtils.*;

/**
 * The {@code TerminalSession} class represents a terminal session bound to a single logged-in user.
 */
public class TerminalSession implements Session {
    /**
     * The main {@link Scanner} instance for this program
     */
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final LanguageManager LANGUAGE_MANAGER = LanguageManager.getInstance();
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
        LANGUAGE_MANAGER.setLocale(Configuration.DEFAULT_LANGUAGE);
        return new TerminalSession();
    }

    @Override
    public void onLogin(User user) {
        Locale preferredLanguage = user.getPreferences().getPreferredLanguage();
        if (preferredLanguage != null) LANGUAGE_MANAGER.setLocale(preferredLanguage);

        // Dashboard menu
        new Menu(
                new Menu.MenuItem("menu_accounts", () -> {
                    // Accounts menu
                    new Menu(
                            new Menu.MenuItem("menu_select_account", () -> {
                                chooseOne(user.getAccountsAvailable().stream().sorted().toArray(Account[]::new), Account::getDisplayName, account -> {
                                    account.logAccess(user);
                                    // Account menu
                                    new Menu(
                                            new Menu.MenuItem("menu_make_transaction", () -> handleMakePayment(account, user)),
                                            new Menu.MenuItem("menu_view_transaction_history", () -> handleViewTransactionHistory(
                                                    account, Transaction.CHRONOLOGICAL, "order1")
                                            ),
                                            new Menu.MenuItem("menu_view_transaction_history2", () -> handleViewTransactionHistory(
                                                    account, Transaction.AMOUNT, "order2")
                                            ),
                                            new Menu.MenuItem("menu_open_audit_log", () -> handleViewAuditLog(account.getAuditLog())),
                                            new Menu.MenuItem("menu_settings", () -> {
                                                // Account settings menu
                                                new Menu(
                                                        new Menu.MenuItem("menu_account_settings_change_name", () -> handleChangeAccountName(user, account)),
                                                        new Menu.MenuItem("menu_account_settings_add_owner", () -> handleAddOwner(user, account)),
                                                        Menu.getBackItem()
                                                ).prompt();
                                            }),
                                            new Menu.MenuItem("menu_close_account", () -> handleCloseAccount(account, user)),
                                            Menu.getBackItem()
                                    ).prompt(() -> accountMenuBeforeEach(account));
                                });
                            }),
                            new Menu.MenuItem("menu_open_new_account", () -> handleOpenAccount(user)),
                            Menu.getBackItem()
                    ).prompt(() -> accountsMenuBeforeEach(user));
                }),
                new Menu.MenuItem("menu_settings", () -> {
                    // User preferences menu
                    new Menu(
                            new Menu.MenuItem("menu_settings_language", () -> handleChangePreferredLanguage(user)),
                            Menu.getBackItem()
                    ).prompt();
                }),
                new Menu.MenuItem("menu_show_users", this::handleViewUsers).setVisible(user.isAdmin()),
                new Menu.MenuItem("menu_show_accounts", this::handleViewAccounts).setVisible(user.isAdmin()),
                new Menu.MenuItem("menu_add_user", this::handleRegisterUser).setVisible(user.isAdmin()),
                new Menu.MenuItem("menu_logout").exitMenuAfter(),
                new Menu.MenuItem("menu_logout_exit", this::endSession).exitMenuAfter()
        ).prompt(() -> dashboardMenuBeforeEach(user));

        onLogout(user);
    }

    @Override
    public void onLogout(User user) {
        // Revert the language back to the system defaults
        LANGUAGE_MANAGER.setLocale(Configuration.DEFAULT_LANGUAGE);

        // Save user data
        Utils.printDebugMessage("\nSaving user data\n");
        user.save();

        System.out.println(LANGUAGE_MANAGER.getString(isActive() ? "user_logout" : "user_logout_exit"));
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
     * User registration handler
     */
    public Menu.MenuItem.Result handleRegisterUser() {
        // Generic user data
        String name = promptStringMandatory(LANGUAGE_MANAGER.getString("register_name"), LANGUAGE_MANAGER);
        String lastName = promptStringMandatory(LANGUAGE_MANAGER.getString("register_last_name"), LANGUAGE_MANAGER);
        String username = promptStringMandatory(LANGUAGE_MANAGER.getString("register_username"), LANGUAGE_MANAGER);
        String email = promptStringValidated(LANGUAGE_MANAGER.getString("register_email"),
                s -> Pattern.matches("^[a-zA-Z\\d_.+-]+@[a-zA-Z\\d-]+\\.[a-zA-Z\\d-.]+$", s),
                () -> System.out.println(LANGUAGE_MANAGER.getString("register_email_invalid")));

        // Password
        boolean passwordValid;
        String password;
        do {
            password = promptStringMandatory(LANGUAGE_MANAGER.getString("register_password"), LANGUAGE_MANAGER);
            String passwordAgain = promptStringMandatory(LANGUAGE_MANAGER.getString("register_password_again"), LANGUAGE_MANAGER);
            passwordValid = password.equals(passwordAgain);
            if (!passwordValid) System.out.println(LANGUAGE_MANAGER.getString("register_password_dont_match"));
        } while (!passwordValid);

        // Admin?
        AtomicBoolean admin = new AtomicBoolean();
        new Menu(
                new Menu.MenuItem("option_yes", () -> admin.set(true)).exitMenuAfter(),
                new Menu.MenuItem("option_no", () -> admin.set(false)).exitMenuAfter()
        ).prompt(() -> System.out.println(LANGUAGE_MANAGER.getString("register_admin")));

        User user = new User(username, email, name, lastName, password, admin.get());
        user.save();
        Bank.users.add(user);
        return new Menu.MenuItem.Result(false, LANGUAGE_MANAGER.getString("register_done", "fullName", user.getFullName()));
    }

    /**
     * Open account handler
     *
     * @param user user
     */
    private Menu.MenuItem.Result handleOpenAccount(User user) {
        // If the user is over the limit
        if (user.getAccounts().size() >= Configuration.USER_MAX_ACCOUNTS)
            return new Menu.MenuItem.Result(false, LANGUAGE_MANAGER.getString("account_open_limit"));
        // Choose account type
        AtomicReference<Account> accountAtomic = new AtomicReference<>();
        chooseOne(Account.Type.values(), null, type -> {
            clear();
            // Choosing the type of currency
            Currency currency;
            do {
                Currency proposed = LanguageManager.getCurrency(user.getPreferences().getPreferredLanguage());
                if (proposed != null) {
                    String input = promptString(LANGUAGE_MANAGER.getString("account_open_currency_default", "currency", proposed.getDisplayName()));
                    currency = input.isEmpty() ? proposed : LanguageManager.getCurrency(input.toUpperCase());
                } else {
                    currency = LanguageManager.getCurrency(promptString(LANGUAGE_MANAGER.getString("account_open_currency")).toUpperCase());
                }
                if (currency == null) System.out.println(LANGUAGE_MANAGER.getString("invalid_currency"));
            } while (currency == null);
            clear();
            // Coming up with the account name
            String defaultAccountName = Utils.getDefaultAccountName(type, user);
            String name = promptString(LANGUAGE_MANAGER.getString("account_open_name", "default", defaultAccountName));
            if (name.isEmpty()) name = defaultAccountName;
            Account account = new Account(type, currency, user, name);
            Bank.accounts.add(account);
            account.save();
            accountAtomic.set(account);
        });
        // If the user cancelled at the type choice menu, exit to last menu and say nothing
        if (accountAtomic.get() == null) return new Menu.MenuItem.Result(false, null);
        Account account = accountAtomic.get();
        return new Menu.MenuItem.Result(false, LANGUAGE_MANAGER.getString("account_opened", Map.of(
                "type", account.getType(),
                "name", account.getName()
        )));
    }

    /**
     * User preferred language change handler
     *
     * @param user user
     */
    private Menu.MenuItem.Result handleChangePreferredLanguage(User user) {
        AtomicReference<Locale> preferredLanguage = new AtomicReference<>();
        chooseOne(LanguageManager.SUPPORTED_LANGUAGES.toArray(Locale[]::new),
                locale -> locale.getDisplayName() + (user.getPreferences().getPreferredLanguage(locale) ? " - " + LANGUAGE_MANAGER.getString("current") : ""),
                locale -> {
                    if (!user.getPreferences().getPreferredLanguage(locale)) {
                        preferredLanguage.set(locale);
                        user.getPreferences().setPreferredLanguage(locale);
                        LANGUAGE_MANAGER.setLocale(locale);
                    }
                }
        );
        if (preferredLanguage.get() == null) return new Menu.MenuItem.Result(false, null);
        user.save();
        return new Menu.MenuItem.Result(false, LANGUAGE_MANAGER.getString("preferred_language_set", "language", preferredLanguage.get().getDisplayName()));
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
            potentialUsers = Bank.users.search(promptString(LANGUAGE_MANAGER.getString("search")));
            if (potentialUsers.length == 0) System.out.println(LANGUAGE_MANAGER.getString("error_user_not_found"));
            else {
                // if search is left empty, exit
                if (potentialUsers[0] == null) return new Menu.MenuItem.Result(false, null);

                chooseOne(potentialUsers, null, user -> {
                    if (account.addOwner(loggedInUser, user, LANGUAGE_MANAGER.getString("account_owner_added", "owner", user.getFullName()))) {
                        result.set(LANGUAGE_MANAGER.getString("account_owner_added", "owner", user.getFullName()));
                    } else {
                        result.set(LANGUAGE_MANAGER.getString("account_owner_already_in", "owner", user.getFullName()));
                    }
                });
            }
        } while (potentialUsers.length == 0);
        account.save();
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
        showSet(account.getTransactionHistory(),
                null,
                order,
                orderLabel, "account_showing_transactions");
    }

    /**
     * View audit log handler
     *
     * @param auditLog account audit log
     */
    private void handleViewAuditLog(AccountAuditLog auditLog) {
        showSet(auditLog.eventList(),
                null,
                AuditEvent.CHRONOLOGICAL,
                "order1", "account_showing_audit_log");
    }

    /**
     * View users handler
     */
    private void handleViewUsers() {
        showSet(Bank.users.all(),
                User::toString,
                null,
                "order3", "showing_users");
    }

    /**
     * View accounts handler
     */
    private void handleViewAccounts() {
        showSet(Bank.accounts.all(),
                account -> account.getDisplayName() + (account.isClosed() ? " [%s]".formatted(LANGUAGE_MANAGER.getString("closed")) : ""),
                null,
                "order4", "showing_accounts");
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
        account.changeName(user, name, LANGUAGE_MANAGER.getString("account_name_changed", "name", name));
        account.save();
        return new Menu.MenuItem.Result(false, LANGUAGE_MANAGER.getString("account_name_changed", "name", name));
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
                    boolean closureResult = account.close(user);
                    result.set(closureResult);
                    // Only add a message if the account couldn't be closed - success message wouldn't show up anyway, because
                    // we're travelling down the menu tree
                    resultMessage.set(closureResult ? null : LANGUAGE_MANAGER.getString("account_close_failed"));
                }).exitMenuAfter(),
                Menu.getBackItem()
        ).prompt(() -> System.out.println(LANGUAGE_MANAGER.getString("account_close_confirmation")));
        account.save();
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
            potentialAccounts = Bank.accounts.search(promptString(LANGUAGE_MANAGER.getString("search")));

            if (potentialAccounts.length == 0)
                System.out.println(LANGUAGE_MANAGER.getString("error_account_not_found"));
            else {
                // if search was left empty, exit
                if (potentialAccounts[0] == null) return new Menu.MenuItem.Result(false, null);

                // Removing the current account from the selection - let's not allow users to send money to the same account
                List<Account> b = new ArrayList<>(Arrays.asList(potentialAccounts));
                b.removeIf(potentialAccount -> potentialAccount.equals(account));
                potentialAccounts = b.toArray(Account[]::new);

                chooseOne(potentialAccounts, null, receiverAccount -> {
                    clear();
                    float amount = (float) promptNumericDouble(LANGUAGE_MANAGER.getString("amount") + ": ", LANGUAGE_MANAGER);
                    clear();
                    System.out.println(LANGUAGE_MANAGER.getString("account_transaction_confirmation", Map.of(
                            "amount", new Balance(account.getCurrency(), amount),
                            "account", receiverAccount
                    )));
                    new Menu(
                            new Menu.MenuItem("menu_authorize_transaction", () -> {
                                // Was the payment successful?
                                if (!account.authorizePayment(amount, receiverAccount, user)) {
                                    message.set(LANGUAGE_MANAGER.getString("account_insufficient_funds"));
                                } else {
                                    message.set(LANGUAGE_MANAGER.getString("account_transaction_successful"));
                                    receiverAccount.save();
                                }
                            }).exitMenuAfter(),
                            Menu.getBackItem()
                    ).dontClear().prompt();
                });
            }
        } while (potentialAccounts.length == 0);
        account.save();
        return new Menu.MenuItem.Result(false, message.get());
    }

    /**
     * Print out all available accounts and a header each accounts menu iteration
     *
     * @param user logged-in user
     */
    private void accountsMenuBeforeEach(User user) {
        List<Account> accounts = user.getAccountsAvailable().stream().sorted().toList();
        System.out.println(
                center(TerminalUtils.getAccountsOverview(accounts), printHeaderAndGetWidth(LANGUAGE_MANAGER.getString("menu_header_accounts")))
        );
        accounts.forEach(account -> System.out.println(account.getDisplayName()));
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
                        LANGUAGE_MANAGER.getString("balance"),
                        account.getBalance(),
                        LANGUAGE_MANAGER.getString("owners"),
                        account.getOwners().size()),
                printHeaderAndGetWidth(account.getName(false)));
        System.out.println(balanceAndOwnerCount);
        AccessAuditEvent lastAccess = account.getAuditLog().getLastAccess();
        System.out.println(center(String
                        .format(LANGUAGE_MANAGER.getString("last_access")
                                + ": %s", lastAccess == null ? LANGUAGE_MANAGER.getString("account_last_access_empty") : lastAccess.toStringShort()),
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
                LANGUAGE_MANAGER.getString("user_welcome", "user", user.getFullName()),
                printHeaderAndGetWidth(LANGUAGE_MANAGER.getString("menu_header_dashboard"))
        ));
    }

    /**
     * Prompt the user for a username
     *
     * @return normalized user login
     */
    public String promptUsername() {
        return promptString(LANGUAGE_MANAGER.getString("login_prompt_username")).trim().split(" ")[0];
    }

    /**
     * Prompt the user for a password
     *
     * @return normalized user password
     */
    public String promptPassword() {
        return promptString(LANGUAGE_MANAGER.getString("login_prompt_password")).trim().split(" ")[0];
    }

    /**
     * User not found error
     *
     * @param login user login that hasn't been found
     */
    public void userNotFound(String login) {
        System.out.printf("%s\n", LANGUAGE_MANAGER.getString("login_user_not_found", "user", login));
    }

    /**
     * Wrong password error
     *
     * @param triesLeft tries left before timeout
     */
    public void wrongPassword(int triesLeft) {
        System.out.printf("%s\n", LANGUAGE_MANAGER.getString(triesLeft == 0 ? "login_wrong_password_timeout" : "login_wrong_password", "tries", String.valueOf(triesLeft)));
    }

    /**
     * If a user tries to log in but is still on timeout, this method is called
     *
     * @param timeLeft time left before begin able to log in again
     */
    public void stillOnTimeout(long timeLeft) {
        long secondsLeft = Duration.ofMillis(timeLeft).toSeconds();
        System.out.printf("%s\n", LANGUAGE_MANAGER.getString("login_still_timeout")
                .replace("{duration}", String.format("%dh%02dm%02ds", secondsLeft / 3600, (secondsLeft % 3600) / 60, (secondsLeft % 60))));
    }
}
