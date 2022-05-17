package bohac.entity.account;

import bohac.Bank;
import bohac.entity.User;
import bohac.storage.JSONSerializable;
import bohac.ui.TerminalUtils;
import bohac.util.Utils;
import bohac.auditlog.AccountAuditLog;
import bohac.auditlog.AuditEvent;
import bohac.auditlog.GenericAuditEvent;
import bohac.auditlog.ModificationAuditEvent;
import bohac.transaction.Transaction;
import org.json.JSONObject;

import java.util.*;

public class Account implements JSONSerializable {
    public static final String FILE_NAME = "accounts.json";
    public static final List<Account> DEFAULT_ACCOUNTS = List.of(
            new Account(Type.CHECKING_ACCOUNT, Currency.getInstance("CZK"))
    );

    public enum Type {
        SAVINGS_ACCOUNT, CHECKING_ACCOUNT, RETIREMENT_ACCOUNT
    }

    private final UUID id;
    private final Type type;
    private final Currency currency;
    private final AccountAuditLog auditLog;
    private final List<Transaction> transactionHistory;
    private final Set<User> owners;
    private String name;
    private float balance;

    public Account(UUID id, Type type, Currency currency, AccountAuditLog auditLog, List<Transaction> transactionHistory, Set<User> owners, float balance, String name) {
        this.id = id;
        this.type = type;
        this.currency = currency;
        this.auditLog = auditLog;
        this.owners = owners;
        this.transactionHistory = transactionHistory;
        this.balance = balance;
        this.name = name;
    }

    public Account(Type type, Currency currency, User owner, String name) {
        this(UUID.randomUUID(), type, currency, new AccountAuditLog(), new ArrayList<>(), Set.of(owner), 0, name);
    }

    public Account(Type type, Currency currency, User owner) {
        this(type, currency, owner, Utils.getDefaultAccountName(owner, type));
    }

    public Account(Type type, Currency currency) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.currency = currency;
        this.auditLog = new AccountAuditLog();
        this.owners = new HashSet<>();
        this.transactionHistory = new ArrayList<>();
    }

    public float getBalanceAmount() {
        return balance;
    }

    public UUID getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public AccountAuditLog getAuditLog() {
        return auditLog;
    }

    public String getName(boolean complete) {
        if (name.isBlank()) {
            return String.format("%s (%s)", type, complete ? id : TerminalUtils.minimize(id.toString(), 5));
        }
        return name;
    }

    public Balance getBalance() {
        return new Balance(currency, balance);
    }

    public String getDisplayName() {
        return getDisplayName(true);
    }

    public String getDisplayName(boolean complete) {
        return String.format("%s - %s", getName(complete), getBalance());
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("type", type)
                .put("currency", currency)
                .put("balance", balance)
                .put("transaction_history", transactionHistory)
                .put("audit_log", auditLog.toJSON())
                .put("name", name)
                .put("owners", owners.stream().map(user -> user.getId().toString()).toList());
    }

    public static Account load(JSONObject object) {
        Utils.printDebugMessage("debug_account_load", "account", object.getString("id"));
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
                    case ACCESS, CLOSURE, CREATION ->
                            accountAuditLog.addEvent(GenericAuditEvent.load((JSONObject) event));
                    case MODIFICATION -> accountAuditLog.addEvent(ModificationAuditEvent.load((JSONObject) event));
                }
            });
        }

        return new Account(UUID.fromString(object.getString("id")),
                Type.valueOf(object.getString("type")),
                Currency.getInstance(object.getString("currency")),
                accountAuditLog,
                transactions,
                owners,
                object.getFloat("balance"),
                object.getString("name"));
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", type=" + type +
                ", currency=" + currency +
                ", auditLog=" + auditLog +
                ", transactionHistory=" + transactionHistory +
                ", owners=" + owners +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
