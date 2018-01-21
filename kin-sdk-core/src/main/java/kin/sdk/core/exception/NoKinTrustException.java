package kin.sdk.core.exception;


import android.support.annotation.NonNull;

public class NoKinTrustException extends OperationFailedException {

    private final String accountId;

    public NoKinTrustException(@NonNull String accountId) {
        super("Account " + accountId + " has no Kin trust-line");

        this.accountId = accountId;
    }

    @NonNull
    public String getAccountId() {
        return accountId;
    }
}
