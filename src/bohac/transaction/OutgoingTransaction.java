package bohac.transaction;

import bohac.Bank;
import bohac.entity.account.Account;
import bohac.entity.User;
import bohac.entity.account.Balance;
import bohac.ui.TerminalSession;
import bohac.util.Utils;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an outgoing transaction
 */
public final class OutgoingTransaction implements Transaction {
    private final UUID receiverID;
    private final User user;
    private final LocalDateTime dateTime;
    private final float amount;
    private final Currency currency;
    private Account receiver;

    /**
     * This constructor is used when loading data from the disk
     */
    public OutgoingTransaction(UUID userID, UUID receiverID, LocalDateTime dateTime, float amount, Currency currency) {
        this.user = Bank.users.getByID(userID).orElse(null);
        this.receiverID = receiverID;
        this.dateTime = dateTime;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * This constructor is called when a user authorizes a transaction
     */
    public OutgoingTransaction(User user, Account receiver, Account sender, float amount, Currency currency) {
        this.user = user;
        this.receiver = receiver;
        this.receiverID = receiver.getId();
        this.dateTime = LocalDateTime.now();
        this.amount = amount;
        this.currency = currency;

        receiver.addTransaction(new IncomingTransaction(sender, amount, currency));
    }

    @Override
    public void initializeTarget() {
        this.receiver = Bank.accounts.getByID(receiverID).orElse(null);
    }

    @Override
    public Account getTarget() {
        return receiver;
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
                .put("type", "OUTGOING")
                .put("target", receiverID)
                .put("amount", amount)
                .put("user", user)
                .put("currency", currency)
                .put("date_time", dateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(dateTime)));
    }

    @Override
    public String toString() {
        return "<- OUTGOING <- " + TerminalSession.languageManager.getString("account_outgoing_transaction", Map.of(
                "amount", new Balance(currency, amount),
                "account", receiverID,
                "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT),
                "user", user.getUsername()
        ));
    }
}
