package kin.sdk.core.exception;/**/

public class AccountDeletedException extends OperationFailedException {

    public AccountDeletedException() {
        super("Account deleted, Create new account");
    }
}
