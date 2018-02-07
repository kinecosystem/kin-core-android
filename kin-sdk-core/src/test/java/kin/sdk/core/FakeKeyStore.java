package kin.sdk.core;


import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.stellar.sdk.KeyPair;

/**
 * Fake KeyStore for testing, implementing naive in memory store
 */
class FakeKeyStore implements KeyStore {

    private List<Account> accounts;

    FakeKeyStore(List<Account> preloadedAccounts) {
        accounts = new ArrayList<>(preloadedAccounts);
    }

    FakeKeyStore() {
        accounts = new ArrayList<>();
    }

    @Override
    public void deleteAccount(int index) {
        accounts.remove(index);
    }

    @NonNull
    @Override
    public List<Account> loadAccounts() {
        return accounts;
    }

    @Override
    public Account newAccount() {

        KeyPair keyPair = KeyPair.random();
        Account account = new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
        accounts.add(account);
        return account;
    }

    @Override
    public KeyPair decryptAccount(Account account) {
        return KeyPair.fromSecretSeed(account.getEncryptedSeed());
    }

    @Override
    public void clearAllAccounts() {
        accounts.clear();
    }
}
