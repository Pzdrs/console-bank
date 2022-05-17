package bohac.entity.account;

import java.util.Currency;

public record Balance(Currency currency, float balance) {

    @Override
    public String toString() {
        return String.format("%.2f %s", balance, currency.getCurrencyCode());
    }
}
