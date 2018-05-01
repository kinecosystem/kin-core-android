package kin.core;

import android.support.annotation.NonNull;
import java.io.IOException;
import kin.core.ServiceProvider.KinAsset;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.OperationFailedException;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.HttpResponseException;
import org.stellar.sdk.responses.SubmitTransactionResponse;

class AccountActivator {

    //unlimited trust, The largest amount unit possible in Stellar
    //see https://www.stellar.org/developers/guides/concepts/assets.html
    private static final String TRUST_NO_LIMIT_VALUE = "922337203685.4775807";
    private final Server server; //horizon server
    private final KinAsset kinAsset;

    AccountActivator(Server server, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
    }

    void activate(@NonNull KeyPair account) throws OperationFailedException {
        verifyParams(account);
        AccountResponse accountResponse;
        try {
            accountResponse = getAccountDetails(account);
            if (kinAsset.hasKinTrust(accountResponse)) {
                return;
            }
            SubmitTransactionResponse response = sendAllowKinTrustOperation(account, accountResponse);
            handleTransactionResponse(response);
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(account.getAccountId());
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    private void verifyParams(@NonNull KeyPair account) {
        Utils.checkNotNull(account, "account");
    }

    @NonNull
    private AccountResponse getAccountDetails(@NonNull KeyPair account) throws IOException, OperationFailedException {
        AccountResponse accountResponse;
        accountResponse = server.accounts().account(account);
        if (accountResponse == null) {
            throw new OperationFailedException("can't retrieve data for account " + account.getAccountId());
        }
        return accountResponse;
    }

    private SubmitTransactionResponse sendAllowKinTrustOperation(KeyPair account, AccountResponse accountResponse)
        throws IOException {
        Transaction allowKinTrustTransaction = new Transaction.Builder(accountResponse).addOperation(
            new ChangeTrustOperation.Builder(kinAsset.getStellarAsset(), TRUST_NO_LIMIT_VALUE)
                .build()
        )
            .build();
        allowKinTrustTransaction.sign(account);
        return server.submitTransaction(allowKinTrustTransaction);
    }

    private void handleTransactionResponse(SubmitTransactionResponse response) throws OperationFailedException {
        if (response == null) {
            throw new OperationFailedException("can't get transaction response");
        }
        if (!response.isSuccess()) {
            throw Utils.createTransactionException(response);
        }
    }
}
