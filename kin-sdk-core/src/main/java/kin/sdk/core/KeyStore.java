package kin.sdk.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;
import org.stellar.sdk.KeyPair;

interface KeyStore {

    void deleteAccount(Account keyPair, String passphrase);

    @NonNull
    List<Account> loadAccounts();

    Account newAccount(String passphrase);

    @Nullable
    String exportAccount(@NonNull Account account, @NonNull String passphrase);

    KeyPair decryptAccount(Account account, String passphrase);
}
