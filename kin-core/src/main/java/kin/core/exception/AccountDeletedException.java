package kin.core.exception;

import kin.core.KinClient;

/**
 * Account was deleted using {@link KinClient#deleteAccount(int)}, and cannot be used any more.
 */
public class AccountDeletedException extends OperationFailedException {

    public AccountDeletedException() {
        super("Account deleted, Create new account");
    }
}
