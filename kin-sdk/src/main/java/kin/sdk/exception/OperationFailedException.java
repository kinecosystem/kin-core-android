package kin.sdk.exception;

public class OperationFailedException extends Exception {

    public OperationFailedException(Throwable cause) {
        super(cause);
    }

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Exception cause) {
        super(message, cause);
    }
}
