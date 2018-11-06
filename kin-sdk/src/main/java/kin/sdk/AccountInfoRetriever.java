package kin.sdk;


import android.support.annotation.NonNull;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.Environment.KinAsset;
import kin.sdk.exception.AccountNotActivatedException;
import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.OperationFailedException;
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
