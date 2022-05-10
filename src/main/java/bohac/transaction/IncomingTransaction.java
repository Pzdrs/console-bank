package bohac.transaction;

import bohac.Account;

import java.time.LocalDateTime;

public record IncomingTransaction(Account target, LocalDateTime dateTime, float amount) implements Transaction {
}
