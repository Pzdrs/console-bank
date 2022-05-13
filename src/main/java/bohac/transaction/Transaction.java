package bohac.transaction;

import bohac.entity.Account;

import java.time.LocalDateTime;

public interface Transaction {
    Account target();

    LocalDateTime dateTime();

    float amount();
}
