package kin.sdk.core.exception;

public class PassphraseException extends Exception {

    public PassphraseException() {
        super("Wrong passphrase - could not decrypt key with given passphrase");
    }
}

