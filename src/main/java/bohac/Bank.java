package bohac;

import java.util.Currency;
import java.util.Set;
import java.util.UUID;

public class Bank {
    public static void main(String[] args) {
        User u1 = new User(UUID.randomUUID(), "Petr", "Bohac", "petrbohac3@seznam.cz", "123", null);
        User u2 = new User(UUID.randomUUID(), "Marcel", "Horvath", "horvad@seznam.cz", "abc", null);
        User u3 = new User(UUID.randomUUID(), "Adam", "Sucharda", "suchar@seznam.cz", "a1c", null);

        Account account1 = new Account(Account.Type.CHECKING_ACCOUNT, Currency.getInstance("CZK"), Set.of(u1));
        Account account2 = new Account(Account.Type.CHECKING_ACCOUNT, Currency.getInstance("CZK"), Set.of(u2, u3));

        System.out.println(account1);
        System.out.println(account2);
    }
}
