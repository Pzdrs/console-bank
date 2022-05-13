package bohac.transaction;

import bohac.entity.Account;
import bohac.entity.User;

import java.time.LocalDateTime;

public record OutgoingTransaction(User user, Account target, LocalDateTime dateTime,
                                  float amount) implements Transaction {
}
