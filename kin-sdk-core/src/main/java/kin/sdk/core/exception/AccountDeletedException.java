package kin.sdk.core.exception;

import kin.sdk.core.KinClient;

/**
 * Account was deleted using {@link KinClient#deleteAccount(int, String)}, and cannot be used any more.
 */
public class AccountDeletedException extends OperationFailedException {

    public AccountDeletedException() {
        super("Account deleted, Create new account");
    }
}
