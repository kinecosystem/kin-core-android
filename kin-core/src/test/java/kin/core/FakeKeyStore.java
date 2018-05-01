package kin.core;


import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.stellar.sdk.KeyPair;

/**
 * Fake KeyStore for testing, implementing naive in memory store
 */
class FakeKeyStore implements KeyStore {

    private List<KeyPair> accounts;

    FakeKeyStore(List<KeyPair> preloadedAccounts) {
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
    public List<KeyPair> loadAccounts() {
        return accounts;
    }

    @Override
    public KeyPair newAccount() {
        KeyPair account = KeyPair.random();
        accounts.add(account);
        return account;
    }

    @Override
    public void clearAllAccounts() {
        accounts.clear();
    }
}
