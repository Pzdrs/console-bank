package bohac;

import bohac.entity.Account;
import bohac.entity.User;
import bohac.transaction.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;

public class AccountList implements Iterable<Account> {
    private List<Account> accounts;
    public AccountList() {
        this.accounts = new ArrayList<>();
    }

    public Optional<Account> getByID(UUID uuid) {
        return accounts.stream().filter(account -> account.getId().equals(uuid)).findFirst();
    }

    public void add(Account account) {
        accounts.add(account);
    }

    public void initializeTransactions() {
        for (Account account : accounts) {
            account.getTransactionHistory().forEach(Transaction::initializeTarget);
        }
    }

    public static AccountList load(String path) {
        AccountList accounts = new AccountList();
        File file = new File(path);
        if (!file.exists()) {
            createDefaultFile(path);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            array.forEach(account -> accounts.add(Account.load((JSONObject) account)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return accounts;
    }

    private static void createDefaultFile(String path) {
        JSONArray defaultAccounts = new JSONArray();
        Account.DEFAULT_ACCOUNTS.forEach(account -> defaultAccounts.put(account.toJSON()));
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(defaultAccounts.toString());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create the default accounts.json file");
        }
    }

    @Override
    public Iterator<Account> iterator() {
        return accounts.iterator();
    }
}
