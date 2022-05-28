package bohac.transaction;

import bohac.Bank;
import bohac.entity.account.Account;
import bohac.entity.account.Balance;
import bohac.ui.TerminalSession;
import bohac.util.Utils;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an incoming transaction
 */
public final class IncomingTransaction implements Transaction {
    private final UUID sender;
    private final LocalDateTime dateTime;
    private final float amount;
    private final Currency currency;

    /**
     * This constructor is used when loading data from the disk
     */
    public IncomingTransaction(UUID senderID, LocalDateTime dateTime, float amount, Currency currency) {
        this.sender = senderID;
        this.dateTime = dateTime;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * This constructor is called when a user authorizes a transaction
     */
    public IncomingTransaction(Account sender, float amount, Currency currency) {
        this.sender = sender.getId();
        this.dateTime = LocalDateTime.now();
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public Account getTarget() {
        return Bank.accounts.getByID(sender).orElse(null);
    }

    @Override
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public float getAmount() {
        return amount;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public JSONObject toJSON() {
        return Transaction.super.toJSON()
                .put("type", "INCOMING");
    }

    @Override
    public String toString() {
        return String.format("-> %s -> ", TerminalSession.LANGUAGE_MANAGER.getString("incoming")) +
                TerminalSession.LANGUAGE_MANAGER.getString("account_incoming_transaction", Map.of(
                        "amount", new Balance(currency, amount),
                        "account", sender,
                        "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT)
                ));
    }
}
