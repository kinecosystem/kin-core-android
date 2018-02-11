package kin.sdk.core;

import static kin.sdk.core.TestUtils.enqueueEmptyResponse;
import static kin.sdk.core.TestUtils.generateSuccessMockResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import kin.sdk.core.ServiceProvider.KinAsset;
import kin.sdk.core.exception.AccountNotActivatedException;
import kin.sdk.core.exception.AccountNotFoundException;
import kin.sdk.core.exception.OperationFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.hamcrest.Matchers;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.HttpResponseException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class BalanceQueryTest {

    private static final String ACCOUNT_ID_KIN_ISSUER = "GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM";
    private static final String ACCOUNT_ID = "GBQUCJ755LJBUFFKFZCTV7XFA6JUR5NAAEJF66SPCN3XROHVKSG3VVUY";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Server server;
    private MockWebServer mockWebServer;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("").toString();

        server = new Server(url);
    }

    @Test
    public void getBalance_Success() throws Exception {
        mockWebServer.enqueue(generateSuccessMockResponse(this.getClass(), "balance_res_success.json"));

        Balance balance = getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);

        Assert.assertEquals("9999.9999800", balance.value().toPlainString());
    }

    @Test
    public void getBalance_VerifyQueryAccountID() throws Exception {
        mockWebServer.enqueue(generateSuccessMockResponse(this.getClass(), "balance_res_success.json"));

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);

        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID));
    }

    @Test
    public void getBalance_NoKinTrust() throws Exception {
        mockWebServer.enqueue(generateSuccessMockResponse(this.getClass(), "balance_res_no_kin_trust.json"));
        expectedEx.expect(AccountNotActivatedException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID)));

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    @Test
    public void getBalance_IOException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    @Test
    public void getBalance_NullResponse() throws Exception {
        enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID);

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    @Test
    public void getBalance_AccountNotExists() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
        );
        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID)));

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    @Test
    public void getBalance_HttpResponseError() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
        );

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(Matchers.<Throwable>instanceOf(HttpResponseException.class));
        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getBalance_NullInput() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
        );
        expectedEx.expect(IllegalArgumentException.class);
        KinAsset kinAsset = new KinAsset(ACCOUNT_ID_KIN_ISSUER);
        BalanceQuery balanceQuery = new BalanceQuery(server, kinAsset);
        balanceQuery.getBalance(null);
    }

    private Balance getBalance(String issuerAccountId, String accountId) throws OperationFailedException {
        KinAsset kinAsset = new KinAsset(issuerAccountId);
        BalanceQuery balanceQuery = new BalanceQuery(server, kinAsset);
        Account account = new Account("", accountId);
        return balanceQuery.getBalance(account);
    }

}