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
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an outgoing transaction
 */
public final class OutgoingTransaction implements Transaction {
    private final UUID receiver;
    private final User user;
    private final LocalDateTime dateTime;
    private final float amount;
    private final Currency currency;

    /**
     * This constructor is used when loading data from the disk
     */
    public OutgoingTransaction(UUID userID, UUID receiverID, LocalDateTime dateTime, float amount, Currency currency) {
        this.user = Bank.users.getByID(userID).orElse(null);
        this.receiver = receiverID;
        this.dateTime = dateTime;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * This constructor is called when a user authorizes a transaction
     */
    public OutgoingTransaction(User user, Account receiver, Account sender, float amount, Currency currency) {
        this.user = user;
        this.receiver = receiver.getId();
        this.dateTime = LocalDateTime.now();
        this.amount = amount;
        this.currency = currency;

        receiver.addTransaction(new IncomingTransaction(sender, amount, currency));
    }

    @Override
    public Account getTarget() {
        return Bank.accounts.getByID(receiver).orElse(null);
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
                .put("type", "OUTGOING")
                .put("user", user.getId());
    }

    @Override
    public String toString() {
        return String.format("<- %s <- ", TerminalSession.LANGUAGE_MANAGER.getString("outgoing")) +
                TerminalSession.LANGUAGE_MANAGER.getString("account_outgoing_transaction", Map.of(
                        "amount", new Balance(currency, amount),
                        "account", receiver,
                        "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT),
                        "user", user.getUsername()
                ));
    }
}
