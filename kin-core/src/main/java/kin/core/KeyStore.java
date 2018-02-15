package kin.core;

import android.support.annotation.NonNull;
import java.util.List;
import kin.core.exception.CreateAccountException;
import kin.core.exception.DeleteAccountException;
import org.stellar.sdk.KeyPair;

interface KeyStore {

    @NonNull
    List<Account> loadAccounts() throws LoadAccountException;

    void deleteAccount(int index) throws DeleteAccountException;

    Account newAccount() throws CreateAccountException;

    KeyPair decryptAccount(Account account) throws CryptoException;

    void clearAllAccounts();
}
