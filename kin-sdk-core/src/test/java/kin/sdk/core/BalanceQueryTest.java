package kin.sdk.core;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import kin.sdk.core.ServiceProvider.KinAsset;
import kin.sdk.core.exception.AccountNotFoundException;
import kin.sdk.core.exception.NoKinTrustException;
import kin.sdk.core.exception.OperationFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.AccountsRequestBuilder;
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
        mockWebServer.enqueue(new MockResponse()
            .setBody(loadResource("balance_res_success.json"))
            .setResponseCode(200)
        );

        Balance balance = getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);

        Assert.assertEquals("9999.9999800", balance.value().toPlainString());
    }

    @Test
    public void getBalance_VerifyQueryAccountID() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setBody(loadResource("balance_res_success.json"))
            .setResponseCode(200)
        );

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);

        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID));
    }

    @Test
    public void getBalance_NoKinTrust() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setBody(loadResource("balance_res_no_kin_trust.json"))
            .setResponseCode(200)
        );
        expectedEx.expect(NoKinTrustException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID)));

        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    @Test
    public void getBalance_IOException() throws Exception {
        server = new Server(mockWebServer.url("").toString() + "/not_real_address");
        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

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
        expectedEx.expectCause(instanceOf(HttpResponseException.class));
        getBalance(ACCOUNT_ID_KIN_ISSUER, ACCOUNT_ID);
    }

    private Balance getBalance(String issuerAccountId, String accountId) throws OperationFailedException {
        KinAsset kinAsset = new KinAsset(issuerAccountId);
        BalanceQuery balanceQuery = new BalanceQuery(server, kinAsset);
        Account account = new Account("", accountId);
        return balanceQuery.getBalance(account);
    }

    private String loadResource(String res) {
        InputStream is = this.getClass().getClassLoader()
            .getResourceAsStream(res);
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}