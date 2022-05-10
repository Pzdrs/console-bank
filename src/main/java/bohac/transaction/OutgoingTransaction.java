package bohac.transaction;

import bohac.Account;
import bohac.User;

import java.time.LocalDateTime;

public record OutgoingTransaction(User user, Account target, LocalDateTime dateTime,
                                  float amount) implements Transaction {
}
