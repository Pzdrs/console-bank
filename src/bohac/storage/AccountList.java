package bohac.storage;

import bohac.Bank;
import bohac.entity.User;
import bohac.entity.account.Account;
import bohac.util.Utils;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * The {@link AccountList} represents a collection of {@link Account} objects
 */
public class AccountList implements Iterable<Account> {
    private final List<Account> accounts;

    public AccountList() {
        this.accounts = new ArrayList<>();
    }

    /**
     * Get an account by its ID
     *
     * @param uuid id
     * @return potentially empty {@code Optional<Account>} object
     */
    public Optional<Account> getByID(UUID uuid) {
        return accounts.stream().filter(account -> account.getId().equals(uuid)).findFirst();
    }

    /**
     * Gets all the accounts associated with a given user
     *
     * @param user user
     * @return array of {@link Account} objects
     */
    public Account[] getUserAccounts(User user) {
        return accounts.stream()
                .filter(account -> account.getOwners().contains(user) && !account.isClosed())
                .sorted().toArray(Account[]::new);
    }

    /**
     * Adds a new account to the account list
     *
     * @param account account object
     */
    public void add(Account account) {
        accounts.add(account);
    }

    /**
     * Initializes transactions
     */
    public void initializeTransactions() {
        for (Account account : accounts) {
            account.initializeTransactions();
        }
    }

    /**
     * Advanced search method
     *
     * @param s searched literal, if empty string is passed in, array of length 1 is returned (the content is null)
     * @return array of {@link Account} objects, could be empty
     */
    public Account[] search(String s) {
        if (s.isEmpty()) return new Account[1];
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
            if (Utils.similarity(account.getName(true), s) > 0.4 && !account.isClosed()) potentialAccounts.add(account);
        }

        return potentialAccounts.toArray(Account[]::new);
    }

    /**
     * Loader method
     *
     * @param path file path
     * @return {@link AccountList} instance
     */
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
}
