package bohac.entity;

import bohac.auditlog.AccountAuditLog;
import bohac.transaction.Transaction;

import java.time.LocalDateTime;
import java.util.*;

public class Account {
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
