package bohac.transaction;

import bohac.Bank;
import bohac.entity.account.Account;
import bohac.entity.User;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class OutgoingTransaction implements Transaction {
    private UUID receiverID;
    private User user;
    private Account receiver;
    private LocalDateTime dateTime;
    private float amount;

    public OutgoingTransaction(UUID userID, UUID receiverID, LocalDateTime dateTime, float amount) {
        this.user = Bank.users.getByID(userID).orElse(null);
        this.receiverID = receiverID;
        this.dateTime = dateTime;
        this.amount = amount;
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
    public JSONObject toJSON() {
        return new JSONObject()
                .put("type", "OUTGOING")
                .put("target", receiverID)
                .put("amount", amount)
                .put("date_time", dateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(dateTime)));
    }

    @Override
    public String toString() {
        return "OutgoingTransaction{" +
                "receiverID=" + receiverID +
                ", user=" + user +
                ", dateTime=" + dateTime +
                ", amount=" + amount +
                '}';
    }
}
