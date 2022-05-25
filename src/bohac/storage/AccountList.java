package bohac.storage;

import bohac.entity.User;
import bohac.util.Utils;
import bohac.entity.account.Account;
import bohac.transaction.Transaction;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class AccountList implements Iterable<Account> {
    private final List<Account> accounts;

    public AccountList() {
        this.accounts = new ArrayList<>();
    }

    public Optional<Account> getByID(UUID uuid) {
        return accounts.stream().filter(account -> account.getId().equals(uuid)).findFirst();
    }

    public List<Account> get() {
        return accounts;
    }

    public void add(Account account) {
        accounts.add(account);
    }

    public void initializeTransactions() {
        for (Account account : accounts) {
            account.initializeTransactions();
        }
    }

    public static AccountList load(Path path) {
        AccountList accounts = new AccountList();
        Utils.loadFile(path.toFile(), objects -> objects.forEach(account -> accounts.add(Account.load((JSONObject) account))), defaultAccounts -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
                writer.write(defaultAccounts.toString());
            } catch (IOException e) {
                throw new RuntimeException(String.format("Couldn't create the default %s file", Account.FILE_NAME));
            }
        }, Account.DEFAULT_ACCOUNTS);
        return accounts;
    }

    @Override
    public Iterator<Account> iterator() {
        return accounts.iterator();
    }

    public Account[] search(String s) {
        // exact id
        try {
            Optional<Account> byID = getByID(UUID.fromString(s));
            if (byID.isPresent()) return new Account[]{byID.get()};
        } catch (IllegalArgumentException ignored) {
            // invalid uuid
        }

        // similar name
        List<Account> potentialAccounts = new ArrayList<>();
        for (Account account : accounts) {
            if (Utils.similarity(account.getName(true), s) > 0.4) potentialAccounts.add(account);
        }

        return potentialAccounts.toArray(Account[]::new);
    }
}
