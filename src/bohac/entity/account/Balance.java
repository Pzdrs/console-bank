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

    /**
     * Balance normalization
     *
     * @return the same object if the currency is the same as the base currency ({@link Configuration#BASE_CURRENCY}),
     * otherwise it converts the value using the Conversion API and returns a new {@link Balance} object
     */
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

    /**
     * Converts a given amount of money from one currency to another using the Conversion API
     *
     * @param amount amount of money to convert
     * @param from   from currency
     * @param to     to currency
     * @return the {@code amount} in the {@code to} currency
     */
    public static float convert(float amount, Currency from, Currency to) {
        if (from == to) return amount;
        JSONObject json = Bank.API.getJSON(String.format("/currencies/%s/%s.json",
                from.getCurrencyCode().toLowerCase(), to.getCurrencyCode().toLowerCase()));
        return amount * json.getFloat(to.getCurrencyCode().toLowerCase());
    }
}
