package kin.core.exception;


import android.support.annotation.NonNull;
import kin.core.KinAccount;

/**
 * Account is not activated, use {@link KinAccount#activate()} to activate the account
 */
public class AccountNotActivatedException extends OperationFailedException {

    private final String accountId;

    public AccountNotActivatedException(@NonNull String accountId) {
        super("Account " + accountId + " is not activated");

        this.accountId = accountId;
    }

    @NonNull
    public String getAccountId() {
        return accountId;
    }
}
