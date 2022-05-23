package bohac.entity.account;

import java.util.Currency;

public record Balance(Currency currency, float balance) implements Comparable<Balance> {

    @Override
    public String toString() {
        return String.format("%.2f %s", balance, currency.getCurrencyCode());
    }

    @Override
    public int compareTo(Balance o) {
        // TODO: 5/23/2022 actual comparison using prevody men
        return 0;
    }
}
