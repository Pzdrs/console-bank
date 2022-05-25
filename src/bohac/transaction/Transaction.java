package bohac.transaction;

import bohac.storage.JSONSerializable;
import bohac.util.Utils;
import bohac.entity.account.Account;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

/**
 * A transaction of some amount of money, incoming or outgoing.
 */
public interface Transaction extends Comparable<Transaction>, JSONSerializable {
    Comparator<Transaction> AMOUNT = Comparator.comparing(Transaction::getAmount).reversed();
    Comparator<Transaction> CHRONOLOGICAL = Comparator.comparing(Transaction::getDateTime).reversed();

    /**
     * Transaction type
     */
    enum Type {
        INCOMING, OUTGOING
    }

    /**
     * Initialize data - necessary workaround due to the design of the data structure
     */
    void initializeTarget();

    /**
     * @return the target account associated with this transaction, for an incoming transaction, the target would be the sender account, vice versa
     */
    Account getTarget();

    /**
     * @return a {@link LocalDateTime} object representing the time this transaction took place
     */
    LocalDateTime getDateTime();

    /**
     * @return the amount of money that's being transferred
     */
    float getAmount();

    /**
     * @return the currency that was used for this transaction
     */
    Currency getCurrency();

    /**
     * This method makes sure that a sorted list of transactions is ordered from latest to earliest
     *
     * @param o the object to be compared.
     */
    @Override
    default int compareTo(Transaction o) {
        return CHRONOLOGICAL.compare(o, this);
    }

    /**
     * Loads a {@code Transaction} object from JSON
     *
     * @param object {@link Transaction} object
     * @return the {@link Transaction} object
     */
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
