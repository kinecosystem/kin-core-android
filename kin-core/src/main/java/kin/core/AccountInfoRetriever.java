package kin.core;


import android.support.annotation.NonNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kin.core.Environment.KinAsset;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.OperationFailedException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.RequestBuilder;
import org.stellar.sdk.requests.TransactionsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.HttpResponseException;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.TransactionResponse;

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
     * @param accountId the account ID to get all its transaction history
     * @return the list of payments {@link PaymentInfo}
     * @throws OperationFailedException or one of its subclasses
     */
    List<PaymentInfo> getPaymentsHistory(@NonNull String accountId) throws OperationFailedException {
        List<PaymentInfo> payments;
        payments = getPaymentsHistory(server.transactions(), accountId);

        return payments;
    }

    /**
     * @param requestParams is an optional parameters to the request, such as accountId, cursor, limit, and order.
     *                      If no account is given then using the current account if exist.
     * @return the list of payments {@link PaymentInfo}
     * @throws OperationFailedException or one of its subclasses
     */
    List<PaymentInfo> getPaymentsHistory(PaymentsHistoryRequestParams requestParams, String accountId) throws OperationFailedException {
        List<PaymentInfo> payments;
        TransactionsRequestBuilder transactionBuilder = server.transactions();
        // Add all the optional parameters that the client supplied (if any supplied) to the request
        if (requestParams.getLimit() > 0) {
            transactionBuilder.limit(requestParams.getLimit());
        }
        if (requestParams.getOrder() != null) {
            transactionBuilder.order(RequestBuilder.Order.valueOf(requestParams.getOrder().name()));
        }

        payments = getPaymentsHistory(transactionBuilder, accountId);


        return payments;
    }

    private List<PaymentInfo> getPaymentsHistory(TransactionsRequestBuilder transactions, @NonNull String accountId) throws OperationFailedException {
        List<PaymentInfo> payments = new ArrayList<>();
        try {
            Page<TransactionResponse> response = transactions
                    .forAccount(KeyPair.fromAccountId(accountId))
                    .execute();
            if (response == null || response.getRecords() == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            } else {
                for (TransactionResponse transactionResponse : response.getRecords()) {
                    PaymentInfo paymentInfo = PaymentInfoHelper.extractPaymentInfo(transactionResponse, kinAsset);
                    if (paymentInfo != null) {
                        payments.add(paymentInfo);
                    }
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

        return payments;
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
