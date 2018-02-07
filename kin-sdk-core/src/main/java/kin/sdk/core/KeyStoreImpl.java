package kin.sdk.core;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import org.stellar.sdk.KeyPair;

class KeyStoreImpl implements KeyStore {

    private static final String PREF_NAME = "KinKeyStore";
    private static final String PREF_KEY_SECRET_SEED = "secret_seed";

    private final SharedPreferences sharedPref;

    KeyStoreImpl(Context context) {
        this.sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private void addKeyPair(@NonNull KeyPair keyPair, @NonNull String passphrase) {
        sharedPref.edit()
            .putString(PREF_KEY_SECRET_SEED, String.valueOf(keyPair.getSecretSeed()))
            .apply();
    }

    @Override
    public void deleteAccount(Account keyPair, String passphrase) {
        sharedPref.edit()
            .remove(PREF_KEY_SECRET_SEED)
            .apply();
    }

    @NonNull
    @Override
    public List<Account> loadAccounts() {
        KeyPair keyPair = loadKeyPair();
        if (keyPair != null) {
            return Collections.singletonList(
                new Account(String.valueOf(keyPair.getSecretSeed()), keyPair.getAccountId())
            );
        }
        return Collections.emptyList();
    }

    private KeyPair loadKeyPair() {
        String seed = sharedPref.getString(PREF_KEY_SECRET_SEED, null);
        if (seed != null) {
            return KeyPair.fromSecretSeed(seed);
        }
        return null;
    }

    @Override
    public Account newAccount(String passphrase) {
        KeyPair newAccount = KeyPair.random();
        addKeyPair(newAccount, passphrase);
        return new Account(String.valueOf(newAccount.getSecretSeed()), newAccount.getAccountId());
    }

    @Nullable
    @Override
    public String exportAccount(@NonNull Account account, @NonNull String passphrase) {
        return account.getEncryptedSeed();
    }

    @Override
    public KeyPair decryptAccount(Account account, String passphrase) {
        return KeyPair.fromSecretSeed(account.getEncryptedSeed());
    }
}
