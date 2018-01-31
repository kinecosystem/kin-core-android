package kin.sdk.core.exception;


import android.support.annotation.NonNull;

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
