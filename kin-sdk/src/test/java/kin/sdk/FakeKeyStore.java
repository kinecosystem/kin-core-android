package kin.sdk;


import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.base.KeyPair;

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
    public KeyPair importAccount(@NonNull String json, @NonNull String passphrase)
        throws CryptoException, CreateAccountException {
        return null;
    }

    @Override
    public void clearAllAccounts() {
        accounts.clear();
    }
}
