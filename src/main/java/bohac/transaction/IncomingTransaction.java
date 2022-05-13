package bohac.transaction;

import bohac.entity.Account;

import java.time.LocalDateTime;

public record IncomingTransaction(Account target, LocalDateTime dateTime, float amount) implements Transaction {
}
