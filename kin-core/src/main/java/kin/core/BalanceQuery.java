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

class BalanceQuery {

    private final Server server;
    private final KinAsset kinAsset;

    BalanceQuery(Server server, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
    }

    /**
     * Get balance for the specified account.
     *
     * @param account the {@link KeyPair} to check balance
     * @return the account {@link Balance}
     * @throws AccountNotFoundException if account not created yet
     * @throws AccountNotActivatedException if account has no Kin trust
     * @throws OperationFailedException any other error
     */
    Balance getBalance(@NonNull Account account) throws OperationFailedException {
        Utils.checkNotNull(account, "account");
        Balance balance = null;
        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(account.getAccountId()));
            if (accountResponse == null) {
                throw new OperationFailedException("can't retrieve data for account " + account.getAccountId());
            }
            for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
                if (kinAsset.isKinAsset(assetBalance.getAsset())) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));
                    break;
                }
            }
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(account.getAccountId());
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (balance == null) {
            throw new AccountNotActivatedException(account.getAccountId());
        }

        return balance;
    }
}
