package bohac.transaction;

import bohac.Bank;
import bohac.entity.account.Account;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class IncomingTransaction implements Transaction {
    private UUID senderID;
    private Account sender;
    private LocalDateTime dateTime;
    private float amount;

    public IncomingTransaction(UUID senderID, LocalDateTime dateTime, float amount) {
        this.senderID = senderID;
        this.dateTime = dateTime;
        this.amount = amount;
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
    public JSONObject toJSON() {
        return new JSONObject()
                .put("type", "INCOMING")
                .put("target", senderID)
                .put("amount", amount)
                .put("date_time", dateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(dateTime)));
    }

    @Override
    public String toString() {
        return "IncomingTransaction{" +
                "senderID=" + senderID +
                ", dateTime=" + dateTime +
                ", amount=" + amount +
                '}';
    }
}
