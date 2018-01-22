package kin.sdk.core.exception;

public class OperationFailedException extends Exception {

    public OperationFailedException(Throwable cause) {
        super(cause);
    }

    public OperationFailedException(String message) {
        super(message);
    }
}
