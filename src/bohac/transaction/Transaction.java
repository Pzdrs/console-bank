package bohac.transaction;

import bohac.util.Utils;
import bohac.entity.account.Account;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Transaction {
    enum Type {
        INCOMING, OUTGOING
    }

    void initializeTarget();

    Account getTarget();

    LocalDateTime getDateTime();

    float getAmount();

    JSONObject toJSON();

    static Transaction load(JSONObject object) {
        Type type = Type.valueOf(object.getString("type"));
        LocalDateTime date_time = Utils.parseEpoch(object.getLong("date_time"));
        float amount = object.getFloat("amount");
        if (type == Type.INCOMING) {
            return new IncomingTransaction(UUID.fromString(object.getString("target")), date_time, amount);
        } else if (type == Type.OUTGOING) {
            return new OutgoingTransaction(
                    UUID.fromString(object.getString("user")),
                    UUID.fromString(object.getString("target")),
                    date_time, amount
            );
        } else return null;
    }
}
