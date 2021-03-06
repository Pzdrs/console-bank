package bohac.entity.account;

import bohac.Bank;
import bohac.Configuration;
import bohac.auditlog.AccountAuditLog;
import bohac.auditlog.AuditEvent;
import bohac.auditlog.events.*;
import bohac.entity.User;
import bohac.entity.Entity;
import bohac.transaction.IncomingTransaction;
import bohac.transaction.OutgoingTransaction;
import bohac.transaction.Transaction;
import bohac.ui.TerminalSession;
import bohac.ui.TerminalUtils;
import bohac.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * The {@code Account} class represents a single account.
 */
public final class Account implements Entity, Comparable<Account> {
    /**
     * This comparator compares {@link User} objects by the balance they have
     */
    public static final Comparator<Account> COMPARE_BY_BALANCE = Comparator.comparing(Account::getBalance).reversed();

    /**
     * What name is used for the data folder for this entity
     */
    public static final String DATA_FOLDER = "accounts";
    /**
     * The default accounts that are created when the program runs for the first time
     */
    public static final List<Account> DEFAULT_ACCOUNTS = List.of(
            new Account(Type.SAVINGS_ACCOUNT, Currency.getInstance("CZK"), User.DEFAULT_USERS.get(0), "Adminuv account")
    );

    /**
     * The {@code Type} enum represents the type of {@code Account} instance
     */
    public enum Type {
        SAVINGS_ACCOUNT, CHECKING_ACCOUNT, RETIREMENT_ACCOUNT;

        @Override
        public String toString() {
            return TerminalSession.LANGUAGE_MANAGER.getString(name().toLowerCase());
        }

        public String shortName() {
            return toString().split(" ")[0];
        }
    }

    private final UUID id;
    private final Type type;
    private final Currency currency;
    private final AccountAuditLog auditLog;
    private final List<Transaction> transactionHistory;
    private final Set<User> owners;
    private String name;
    private float balance;
    private boolean closed = false;

    /**
     * This constructor is used, when loading data from the disk
     */
    public Account(UUID id, Type type, Currency currency, AccountAuditLog auditLog, List<Transaction> transactionHistory, Set<User> owners, float balance) {
        this.id = id;
        this.type = type;
        this.currency = currency;
        this.auditLog = auditLog;
        this.owners = owners;
        this.transactionHistory = transactionHistory;
        this.balance = balance;
    }

    /**
     * The minimal constructor - requires the bare minimum to construct a new account
     */
    public Account(Type type, Currency currency) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.currency = currency;
        this.auditLog = new AccountAuditLog();
        this.owners = new HashSet<>();
        this.transactionHistory = new ArrayList<>();
    }

    /**
     * This constructor is used when opening a new account
     */
    public Account(Type type, Currency currency, User owner, String name) {
        this(type, currency);
        this.name = name;
        owners.add(owner);
        auditLog.addEvent(new AccountCreationAuditEvent(owner));
    }

    /**
     * Adds a new owner to the owner list for this account
     *
     * @param loggedInUser who added the co-owner
     * @param owner        the new co-owner
     * @return true if successful, false if the user already is an owner of this account
     */
    public boolean addOwner(User loggedInUser, User owner, String auditRecord) {
        if (owners.contains(owner)) return false;
        owners.add(owner);
        auditLog.addEvent(new ModificationAuditEvent(loggedInUser, String.format(auditRecord, owner.getFullName())));
        return true;
    }

    /**
     * Changes this account's name
     *
     * @param user user who made the change
     * @param name the new name
     */
    public void changeName(User user, String name, String auditRecord) {
        this.name = name;
        auditLog.addEvent(new ModificationAuditEvent(user, auditRecord));
    }

    /**
     * Closes this account
     *
     * @param user who closed the account
     * @return true if the account was closed successfully, false otherwise
     */
    public boolean close(User user) {
        setClosed(true);
        auditLog.addEvent(new AccountClosureEvent(user));
        return true;
    }

    /**
     * This method tries to authorize a transaction
     *
     * @param amount          transaction amount
     * @param receiverAccount transaction target
     * @param user            transaction authorizer
     * @return true if the transaction went through successfully, false otherwise
     */
    public boolean authorizePayment(float amount, Account receiverAccount, User user) {
        if (receiverAccount.equals(this)) return false;
        float fee = Balance.convert(Configuration.TRANSACTION_FEE.balance(), Configuration.TRANSACTION_FEE.currency(), getCurrency());
        if (balance < fee + amount) return false;
        addTransaction(new OutgoingTransaction(user, receiverAccount, this, amount, getCurrency()));
        this.balance -= amount + fee;
        return true;
    }

    /**
     * Add a transaction to this account's history
     *
     * @param transaction transaction
     */
    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
        if (transaction instanceof IncomingTransaction) {
            addFunds(transaction.getAmount(), transaction.getCurrency());
        }
    }

    /**
     * Add funds to this account
     *
     * @param amount   amount of money
     * @param currency currency
     */
    public void addFunds(float amount, Currency currency) {
        if (currency != this.currency) {
            balance += Balance.convert(amount, currency, this.currency);
            return;
        }
        balance += amount;
    }

    /**
     * Adds a new Access event to the account's audit log
     *
     * @param user user who accessed the account
     */
    public void logAccess(User user) {
        auditLog.addEvent(new AccessAuditEvent(user));
        save();
    }

    /**
     * Marks this account as closed
     *
     * @param closed close state
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Sets the account's name
     *
     * @param name new name
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * @param complete whether the method returns a full name or a shortened one - only for accounts without a set name
     * @return the account name
     */
    public String getName(boolean complete) {
        if (name.isBlank()) {
            return String.format("%s (%s)", type, complete ? id : TerminalUtils.minimize(id.toString(), 5));
        }
        return name;
    }

    /**
     * @param complete full or a short version - only for accounts without a set name
     * @return a formatted account display name
     */
    public String getDisplayName(boolean complete) {
        return String.format("%s - %s", getName(complete), getBalance());
    }

    public Balance getBalance() {
        return new Balance(currency, balance);
    }

    public String getDisplayName() {
        return getDisplayName(true);
    }

    public String getName() {
        return getName(true);
    }

    public boolean isClosed() {
        return closed;
    }

    public UUID getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Set<User> getOwners() {
        return new HashSet<>(owners);
    }

    public AccountAuditLog getAuditLog() {
        return auditLog;
    }

    public Currency getCurrency() {
        return currency;
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    /**
     * Saves this instance to the disk
     */
    public void save() {
        Entity.super.save(DATA_FOLDER, id);
    }

    /**
     * Static loader method
     *
     * @param object instance of {@code JSONObject}
     * @return {@code Account} instance
     */
    public static Account load(JSONObject object) {
        List<Transaction> transactions = new ArrayList<>();
        AccountAuditLog accountAuditLog = new AccountAuditLog();
        Set<User> owners = new HashSet<>();

        for (Object owner : object.getJSONArray("owners")) {
            Bank.users.getByID(UUID.fromString(String.valueOf(owner))).ifPresent(owners::add);
        }

        if (object.has("transaction_history"))
            object.getJSONArray("transaction_history").forEach(transaction -> {
                transactions.add(Transaction.load((JSONObject) transaction));
            });

        if (object.has("audit_log")) {
            object.getJSONArray("audit_log").forEach(event -> {
                switch (AuditEvent.Type.valueOf(((JSONObject) event).getString("type"))) {
                    case ACCESS ->
                            accountAuditLog.addEvent(new AccessAuditEvent(GenericAuditEvent.load((JSONObject) event)));
                    case CLOSURE ->
                            accountAuditLog.addEvent(new AccountClosureEvent(GenericAuditEvent.load((JSONObject) event)));
                    case CREATION ->
                            accountAuditLog.addEvent(new AccountCreationAuditEvent(AccountCreationAuditEvent.load((JSONObject) event)));
                    case MODIFICATION -> accountAuditLog.addEvent(ModificationAuditEvent.load((JSONObject) event));
                }
            });
        }


        Account account = new Account(UUID.fromString(object.getString("id")),
                Type.valueOf(object.getString("type")),
                Currency.getInstance(object.getString("currency")),
                accountAuditLog,
                transactions,
                owners,
                object.getFloat("balance"));

        if (object.has("closed") && object.getBoolean("closed")) account.setClosed(true);
        if (object.has("name")) account.setName(object.getString("name"));

        return account;
    }

    @Override
    public JSONObject toJSON() {
        JSONArray transactionHistory = new JSONArray();
        this.transactionHistory.forEach(transaction -> transactionHistory.put(transaction.toJSON()));

        JSONObject account = new JSONObject()
                .put("id", id)
                .put("type", type)
                .put("currency", currency)
                .put("balance", balance)
                .put("transaction_history", transactionHistory)
                .put("audit_log", auditLog.toJSON())
                .put("name", name)
                .put("owners", owners.stream().map(user -> user.getId().toString()).toList());
        if (closed) account.put("closed", true);
        return account;
    }

    @Override
    public String toString() {
        return getName(true) + " - " + id;
    }

    @Override
    public int compareTo(Account o) {
        return COMPARE_BY_BALANCE.compare(o, this);
    }
}
