package kin.sdk.core;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    public void deleteAccount(int index, String passphrase) {
        accounts.remove(index);
    }

    @NonNull
    @Override
    public List<Account> loadAccounts() {
        return accounts;
    }

    @Override
    public Account newAccount(String passphrase) {
        KeyPair keyPair = KeyPair.random();
        Account account = new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
        accounts.add(account);
        return account;
    }

    @Nullable
    @Override
    public String exportAccount(@NonNull Account account, @NonNull String passphrase) {
        return null;
    }

    @Override
    public KeyPair decryptAccount(Account account, String passphrase) {
        return KeyPair.fromSecretSeed(account.getEncryptedSeed());
    }

    @Override
    public void clearAllAccounts() {
        accounts.clear();
    }
}
