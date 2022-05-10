package bohac.transaction;

import bohac.Account;

import java.time.LocalDateTime;

public interface Transaction {
    Account target();

    LocalDateTime dateTime();

    float amount();
}
