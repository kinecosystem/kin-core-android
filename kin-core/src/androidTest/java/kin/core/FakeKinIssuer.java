package kin.core;


import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.stellar.sdk.Asset;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.Transaction.Builder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

/**
 * Fake issuer for integration test, support creating and funding accounts on stellar test net
 */
class FakeKinIssuer {

    private static final int TIMEOUT_SEC = 20;
    private static final String TEST_NETWORK_URL = "https://horizon-testnet.stellar.org";
    private static final String TEST_NETWORK_SCHEME = "https";
    private static final String TEST_NETWORK_HOST = "horizon-testnet.stellar.org";
    private static final String TEST_NETWORK_PATH_SEGMENT = "friendbot";
    private static final String TEST_NETWORK_QUERY_PARAM = "addr";

    private final Server server;
    private final KeyPair issuerKeyPair;
    private final Asset kinAsset;

    FakeKinIssuer() throws IOException {
        this.server = new Server(TEST_NETWORK_URL, TIMEOUT_SEC, TimeUnit.SECONDS);
        this.issuerKeyPair = KeyPair.random();
        this.kinAsset = Asset.createNonNativeAsset("KIN", issuerKeyPair);
        createAndFundWithLumens(issuerKeyPair.getAccountId());
    }

    String getAccountId() {
        return issuerKeyPair.getAccountId();
    }

    private void createAndFundWithLumens(String accountId) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
            .scheme(TEST_NETWORK_SCHEME)
            .host(TEST_NETWORK_HOST)
            .addPathSegment(TEST_NETWORK_PATH_SEGMENT)
            .addQueryParameter(TEST_NETWORK_QUERY_PARAM, accountId)
            .build();
        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
            .url(url).build();
        Response response = client.newCall(request).execute();
        assertTrue(response != null && response.body() != null && response.code() == 200);
    }

    void createAccount(String destinationAccount) throws Exception {
        createAndFundWithLumens(destinationAccount);
    }

    void fundWithKin(String destinationAccount, String amount) throws Exception {
        KeyPair destinationKeyPair = KeyPair.fromAccountId(destinationAccount);
        AccountResponse issuerAccountResponse = server.accounts().account(issuerKeyPair);
        PaymentOperation paymentOperation = new PaymentOperation.Builder(destinationKeyPair, kinAsset, amount)
            .build();
        Transaction transaction = new Builder(issuerAccountResponse)
            .addOperation(paymentOperation)
            .build();
        transaction.sign(issuerKeyPair);
        SubmitTransactionResponse response = server.submitTransaction(transaction);
        if (!response.isSuccess()) {
            throw Utils.createTransactionException(response);
        }
        assertTrue(response.isSuccess());
    }

}
