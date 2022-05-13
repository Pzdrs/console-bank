package bohac.entity;

import bohac.JSONSerializable;
import bohac.Utils;
import bohac.auditlog.AccountAuditLog;
import bohac.transaction.Transaction;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class Account implements JSONSerializable {
    public static final List<User> DEFAULT_ACCOUNTS = List.of(
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
    private float balance;

    public Account(UUID id, Type type, Currency currency, AccountAuditLog auditLog, List<Transaction> transactionHistory, Set<User> owners) {
        this.id = id;
        this.type = type;
        this.currency = currency;
        this.auditLog = auditLog;
        this.owners = owners;
        this.transactionHistory = transactionHistory;
    }

    public Account(Type type, Currency currency, Set<User> owners) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.currency = currency;
        this.auditLog = new AccountAuditLog();
        this.owners = owners;
        this.transactionHistory = new ArrayList<>();
    }

    public boolean makeTransaction(float amount) {
        return false;
    }

    public float getBalance() {
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
                .put("owners", owners.stream().map(user -> user.getId().toString()).toList());
    }

    public static Account load(JSONObject object) {
        System.out.println(Utils.getMessage("debug_account_load").replace("{account}", object.getString("id")));
        List<Transaction> transactions = new ArrayList<>();
        object.getJSONArray("transaction_history").forEach(transaction -> {
            transactions.add(Transaction.load((JSONObject) transaction));
        });
        return new Account(UUID.fromString(object.getString("id")),
                Type.valueOf(object.getString("type")),
                Currency.getInstance(object.getString("currency")),
                null,
                transactions,
                null);
    }

    @Override
    public String toString() {
        return "Account{" +
                "type=" + type +
                ", currency=" + currency +
                ", auditLog=" + auditLog +
                ", transactionHistory=" + transactionHistory +
                ", balance=" + balance +
                ", owners=" + owners +
                '}';
    }
}
