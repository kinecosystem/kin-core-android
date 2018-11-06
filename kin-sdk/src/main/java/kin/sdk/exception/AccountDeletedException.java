package kin.sdk.exception;

import kin.sdk.KinClient;

/**
 * Account was deleted using {@link KinClient#deleteAccount(int)}, and cannot be used any more.
 */
public class AccountDeletedException extends OperationFailedException {

    public AccountDeletedException() {
        super("Account deleted, Create new account");
    }
}
