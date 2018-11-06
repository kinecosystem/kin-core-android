package kin.sdk.exception;

import kin.sdk.KinAccount;
import kin.sdk.KinClient;

/**
 * Decryption/Encryption error when importing - {@link KinClient#importAccount(String, String)} or
 * exporting {@link KinAccount#export(String)} an account.
 */
public class CryptoException extends Exception {

    public CryptoException(Throwable e) {
        super(e);
    }

    public CryptoException(String msg) {
        super(msg);
    }

    public CryptoException(String msg, Throwable e) {
        super(msg, e);
    }
}
