package bohac.transaction;

import bohac.Bank;
import bohac.entity.account.Account;
import bohac.entity.account.Balance;
import bohac.ui.TerminalSession;
import bohac.util.Utils;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an incoming transaction
 */
public final class IncomingTransaction implements Transaction {
    private final UUID senderID;
    private final LocalDateTime dateTime;
    private final float amount;
    private final Currency currency;
    private Account sender;

    /**
     * This constructor is used when loading data from the disk
     */
    public IncomingTransaction(UUID senderID, LocalDateTime dateTime, float amount, Currency currency) {
        this.senderID = senderID;
        this.dateTime = dateTime;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * This constructor is called when a user authorizes a transaction
     */
    public IncomingTransaction(Account sender, float amount, Currency currency) {
        this.sender = sender;
        this.senderID = sender.getId();
        this.dateTime = LocalDateTime.now();
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public void initializeTarget() {
        this.sender = Bank.accounts.getByID(senderID).orElse(null);
    }

    @Override
    public Account getTarget() {
        return sender;
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
        return new JSONObject()
                .put("type", "INCOMING")
                .put("target", senderID)
                .put("amount", amount)
                .put("currency", currency)
                .put("date_time", dateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(dateTime)));
    }

    @Override
    public String toString() {
        return "-> INCOMING -> " + TerminalSession.languageManager.getString("account_incoming_transaction", Map.of(
                "amount", new Balance(currency, amount),
                "account", senderID,
                "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT)
        ));
    }
}
