package kin.core;


import android.support.annotation.NonNull;
import kin.core.exception.CorruptedDataException;
import kin.core.exception.CryptoException;
import org.stellar.sdk.KeyPair;

interface BackupRestore {

    @NonNull
    String exportWallet(@NonNull KeyPair keyPair, @NonNull String passphrase)
        throws CryptoException;

    @NonNull
    KeyPair importWallet(@NonNull String exportedJson, @NonNull String passphrase)
        throws CryptoException, CorruptedDataException;
}
