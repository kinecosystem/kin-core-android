package kin.sdk.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import org.stellar.sdk.KeyPair;

class KeyStore {

    private static final String PREF_NAME = "KinKeyStore";
    private static final String PREF_KEY_SECRET_SEED = "secret_seed";

    private final SharedPreferences sharedPref;

    KeyStore(Context context) {
        this.sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    void addKeyPair(@NonNull KeyPair keyPair, @NonNull String passphrase) {
        sharedPref.edit()
            .putString(PREF_KEY_SECRET_SEED, String.valueOf(keyPair.getSecretSeed()))
            .apply();
    }

    void deleteAccount(EncryptedAccount keyPair, String passphrase) {
        sharedPref.edit()
            .remove(PREF_KEY_SECRET_SEED)
            .apply();
    }

    @NonNull
    List<EncryptedAccount> loadAccounts() {
        KeyPair keyPair = loadKeyPair();
        if (keyPair != null) {
            return Collections.singletonList(
                new EncryptedAccount(String.valueOf(keyPair.getSecretSeed()), keyPair.getAccountId())
            );
        }
        return Collections.emptyList();
    }

    @Nullable
    private KeyPair loadKeyPair() {
        String seed = sharedPref.getString(PREF_KEY_SECRET_SEED, null);
        if (seed != null) {
            return KeyPair.fromSecretSeed(seed);
        }
        return null;
    }


    EncryptedAccount newAccount(String passphrase) {
        KeyPair newAccount = KeyPair.random();
        addKeyPair(newAccount, passphrase);
        return new EncryptedAccount(String.valueOf(newAccount.getSecretSeed()), newAccount.getAccountId());
    }

    @Nullable
    String exportAccount(@NonNull EncryptedAccount account, @NonNull String passphrase) {
        return account.getEncryptedData();
    }

    KeyPair decryptAccount(EncryptedAccount account) {
        return KeyPair.fromSecretSeed(account.getEncryptedData());
    }
}
