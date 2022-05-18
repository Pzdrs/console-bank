package bohac.transaction;

import bohac.Bank;
import bohac.entity.account.Account;
import bohac.entity.User;
import bohac.entity.account.Balance;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.UUID;

public class OutgoingTransaction implements Transaction {
    private UUID receiverID;
    private User user;
    private Account receiver;
    private LocalDateTime dateTime;
    private float amount;
    private Currency currency;

    public OutgoingTransaction(UUID userID, UUID receiverID, LocalDateTime dateTime, float amount, Currency currency) {
        this.user = Bank.users.getByID(userID).orElse(null);
        this.receiverID = receiverID;
        this.dateTime = dateTime;
        this.amount = amount;
        this.currency = currency;
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
                .put("currency", currency)
                .put("date_time", dateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(dateTime)));
    }

    @Override
    public String toString() {
        return String.format("Outgoing transaction of %s to account %s at %s, authorized by %s",
                new Balance(currency, amount), receiverID, dateTime, user.getUsername());
    }
}
