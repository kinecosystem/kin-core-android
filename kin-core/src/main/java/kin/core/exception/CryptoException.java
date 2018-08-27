package kin.core.exception;


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
