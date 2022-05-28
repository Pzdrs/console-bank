package bohac.storage;

import bohac.Bank;
import bohac.Configuration;
import bohac.entity.User;
import bohac.entity.account.Account;
import bohac.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The {@link AccountList} represents a collection of {@link Account} objects
 */
public class AccountList implements Iterable<Account>, JSONSerializableArray {
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
     * Loads all the data from the data folder to memory. Acts as a static factory method also.
     *
     * @param dataFolder dataFolder
     * @return new instance of the {@link AccountList} object with the loaded data
     */
    public static AccountList load(File dataFolder) {
        AccountList accounts = new AccountList();
        if (!dataFolder.exists()) {
            Utils.printDebugMessage("User data folder not found, creating..." + (dataFolder.mkdir() ? "done" : "error"));
        }
        File[] accountFiles = dataFolder.listFiles();
        if (accountFiles != null) {
            if (accountFiles.length == 0) {
                Account.DEFAULT_ACCOUNTS.forEach(account -> {
                    Utils.printDebugMessage("Creating a default account: " + account);
                    accounts.add(account);
                    account.save();
                });
            }
            for (File userFile : accountFiles) {
                Utils.loadFile(userFile, object -> accounts.add(Account.load(object)));
            }
        }
        return accounts;
    }

    @Override
    public Iterator<Account> iterator() {
        return accounts.iterator();
    }

    @Override
    public JSONArray toJSON() {
        JSONArray accounts = new JSONArray();
        this.accounts.forEach(account -> accounts.put(account.toJSON()));
        return accounts;
    }
}
