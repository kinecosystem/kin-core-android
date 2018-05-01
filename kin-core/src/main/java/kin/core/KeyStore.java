package kin.core;

import android.support.annotation.NonNull;
import java.util.List;
import kin.core.exception.CreateAccountException;
import kin.core.exception.DeleteAccountException;
import org.stellar.sdk.KeyPair;

interface KeyStore {

    @NonNull
    List<KeyPair> loadAccounts() throws LoadAccountException;

    void deleteAccount(int index) throws DeleteAccountException;

    KeyPair newAccount() throws CreateAccountException;

    void clearAllAccounts();
}
