package bohac.entity.account;

import bohac.Bank;
import bohac.Configuration;
import org.json.JSONObject;

import java.util.Currency;

/**
 * This object represents an amount/currency pair
 *
 * @param currency
 * @param balance
 */
public record Balance(Currency currency, float balance) implements Comparable<Balance> {
    @Override
    public String toString() {
        return String.format("%.2f %s", balance, currency.getCurrencyCode());
    }


    public Balance normalize() {
        if (currency.getNumericCode() != Configuration.BASE_CURRENCY.getNumericCode()) {
            return new Balance(Configuration.BASE_CURRENCY, convert(balance, currency, Configuration.BASE_CURRENCY));
        }
        return this;
    }

    @Override
    public int compareTo(Balance o) {
        return Float.compare(o.normalize().balance(), this.normalize().balance());
    }

    public static float convert(float amount, Currency from, Currency to) {
        if (from == to) return amount;
        JSONObject json = Bank.API.getJSON(String.format("/currencies/%s/%s.json",
                from.getCurrencyCode().toLowerCase(), to.getCurrencyCode().toLowerCase()));
        return amount * json.getFloat(to.getCurrencyCode().toLowerCase());
    }
}
