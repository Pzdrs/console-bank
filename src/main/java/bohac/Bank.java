package bohac;

import bohac.entity.Account;
import bohac.entity.User;

import java.util.*;

public class Bank {
    public static void main(String[] args) {
        UserList users = new UserList(List.of(
                new User(UUID.randomUUID(), "Petr", "Bohac", "petrbohac3@seznam.cz", "123", null),
                new User(UUID.randomUUID(), "Marcel", "Horvath", "horvad@seznam.cz", "abc", null),
                new User(UUID.randomUUID(), "Adam", "Sucharda", "suchar@seznam.cz", "a1c", null)
        ));

        List<Account> accounts = new ArrayList<>(List.of(
                new Account(Account.Type.CHECKING_ACCOUNT, Currency.getInstance("CZK"),
                        Set.of(users.getById(UUID.randomUUID()))),
                new Account(Account.Type.CHECKING_ACCOUNT, Currency.getInstance("CZK"),
                        Set.of(users.getById(UUID.randomUUID()), users.getById(UUID.randomUUID())))
        ));
        System.out.println(accounts);

        User loggedInUser = users.get(0);
    }
}
