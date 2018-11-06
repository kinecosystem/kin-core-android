package kin.sdk.exception;


import android.support.annotation.NonNull;

/**
 * Account was not created on the blockchain
 */
public class AccountNotFoundException extends OperationFailedException {

    private final String accountId;

    public AccountNotFoundException(@NonNull String accountId) {
        super("Account " + accountId + " was not found");
        this.accountId = accountId;
    }

    @NonNull
    public String getAccountId() {
        return accountId;
    }
}
