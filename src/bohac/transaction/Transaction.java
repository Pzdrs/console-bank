package bohac.transaction;

import bohac.util.Utils;
import bohac.entity.account.Account;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Currency;
import java.util.UUID;

public interface Transaction extends Comparable<Transaction> {
    Comparator<Transaction> AMOUNT = Comparator.comparing(Transaction::getAmount).reversed();
    Comparator<Transaction> CHRONOLOGICAL = Comparator.comparing(Transaction::getDateTime).reversed();

    enum Type {
        INCOMING, OUTGOING
    }

    void initializeTarget();

    Account getTarget();

    LocalDateTime getDateTime();

    float getAmount();

    Currency getCurrency();

    JSONObject toJSON();

    /**
     * This method makes sure that a sorted list of transactions is ordered from latest to earliest
     *
     * @param o the object to be compared.
     */
    @Override
    default int compareTo(Transaction o) {
        return CHRONOLOGICAL.compare(o, this);
    }

    static Transaction load(JSONObject object) {
        Type type = Type.valueOf(object.getString("type"));
        LocalDateTime date_time = Utils.parseEpoch(object.getLong("date_time"));
        float amount = object.getFloat("amount");
        if (type == Type.INCOMING) {
            return new IncomingTransaction(UUID.fromString(object.getString("target")),
                    date_time, amount, Currency.getInstance(object.getString("currency")));
        } else if (type == Type.OUTGOING) {
            return new OutgoingTransaction(
                    UUID.fromString(object.getString("user")),
                    UUID.fromString(object.getString("target")),
                    date_time, amount, Currency.getInstance(object.getString("currency"))
            );
        } else return null;
    }
}
