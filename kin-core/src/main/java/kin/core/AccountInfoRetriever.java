package kin.core;


import android.support.annotation.NonNull;
import java.io.IOException;
import java.math.BigDecimal;
import kin.core.ServiceProvider.KinAsset;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.OperationFailedException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.HttpResponseException;

class AccountInfoRetriever {

    private final Server server;
    private final KinAsset kinAsset;

    AccountInfoRetriever(Server server, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
    }

    /**
     * Get balance for the specified account.
     *
     * @param accountId the account ID to check balance
     * @return the account {@link Balance}
     * @throws AccountNotFoundException if account not created yet
     * @throws AccountNotActivatedException if account has no Kin trust
     * @throws OperationFailedException any other error
     */
    Balance getBalance(@NonNull String accountId) throws OperationFailedException {
        Utils.checkNotNull(accountId, "account");
        Balance balance = null;
        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(accountId));
            if (accountResponse == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            }
            for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
                if (kinAsset.isKinAsset(assetBalance.getAsset())) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));
                    break;
                }
            }
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(accountId);
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (balance == null) {
            throw new AccountNotActivatedException(accountId);
        }

        return balance;
    }

    /**
     * Check if the account has been "burned".
     * @param accountId the account ID to check if it is "burned"
     * @return true if the account is "burned", false otherwise.
     * @throws AccountNotFoundException if account not created yet
     * @throws AccountNotActivatedException if account has no Kin trust
     * @throws OperationFailedException any other error
     */
    boolean isAccountBurned(@NonNull String accountId) throws OperationFailedException {
        Utils.checkNotNull(accountId, "account");
        boolean isBurned;
        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(accountId));
            if (accountResponse == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            }
            validateActivation(accountResponse, accountId);
            AccountResponse.Signer signer = accountResponse.getSigners()[0];
            isBurned = (signer.getWeight() == 0);
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(accountId);
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }

        return isBurned;
    }

    private void validateActivation(@NonNull AccountResponse accountResponse, @NonNull String accountId) throws AccountNotActivatedException {
        boolean isActivated = false;
        for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
            if (kinAsset.isKinAsset(assetBalance.getAsset())) {
                isActivated = true;
            }
        }
        if (!isActivated) {
            throw new AccountNotActivatedException(accountId);
        }
    }

    @AccountStatus
    int getStatus(@NonNull String accountId) throws OperationFailedException {
        try {
            getBalance(accountId);
            return AccountStatus.ACTIVATED;
        } catch (AccountNotFoundException e) {
            return AccountStatus.NOT_CREATED;
        } catch (AccountNotActivatedException e) {
            return AccountStatus.NOT_ACTIVATED;
        }
    }
}
