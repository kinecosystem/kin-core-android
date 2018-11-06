package kin.sdk;

import static junit.framework.Assert.fail;
import static kin.sdk.TestUtils.createKinAsset;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import kin.sdk.Environment.KinAsset;
import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.exception.TransactionFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import kin.base.KeyPair;
import kin.base.Network;
import kin.base.Server;
import kin.base.responses.HttpResponseException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class AccountActivatorTest {

    private static final String ACCOUNT_ID_KIN_ISSUER = "GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM";
    private static final String ACCOUNT_ID_FROM = "GD6QL4QDEOFJQ5W77DY7VHUHK2O4EIM7HDZJDZPOKM7HZ7SJZTKK43WU";
    private static final String SECRET_SEED_FROM = "SCIZAKL7BH7XHL2Y3HZ6SOCMKMTTMHASD5LRHJJKZOHBQARTCFNWAPEF";
    private static final String TX_BODY = "tx=AAAAAP0F8gMjiph23%2Fjx%2Bp6HVp3CIZ848pHl7lM%2BfP5JzNSuAAAAZABq%2FbMAAAAEAAAAAAAAAAAAAAABAAAAAAAAAAYAAAABS0lOAAAAAABBq58xoA5F8Hm%2F7tPH51hBTD4tUsenooq1dLrUnnJnxn%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FAAAAAAAAAAFJzNSuAAAAQCqge87H%2F2FlGekNuQrpmuCWSL%2BgjCue9L1xK0tJ%2BrLdIJB4pydbUNGHWZA0WXExso2%2FaRecFHlOKX4PI8iqjAY%3D";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private KeyStore mockKeyStore;
    private Server server;
    private MockWebServer mockWebServer;
    private KeyPair account;
    private AccountActivator accountActivator;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockServer();
        Network.useTestNetwork();

        KinAsset kinAsset = createKinAsset(ACCOUNT_ID_KIN_ISSUER);
        accountActivator = new AccountActivator(server, kinAsset);
        account = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
    }

    private void mockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("").toString();
        server = new Server(url);
    }

    @Test
    public void activate_Success() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_account_no_kin.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_success.json"));

        accountActivator.activate(account);

        //verify sent requests data
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
    }

    @Test
    public void activate_HasTrustWithDifferentIssuer_Success() throws Exception {
        mockWebServer
            .enqueue(TestUtils
                .generateSuccessMockResponse(this.getClass(), "activate_account_kin_trust_different_issuer.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_success.json"));

        accountActivator.activate(account);

        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
    }

    @Test
    public void activate_HasTrustWithSameIssuerDifferentAsset_Success() throws Exception {
        mockWebServer
            .enqueue(TestUtils
                .generateSuccessMockResponse(this.getClass(),
                    "activate_account_same_issuer_different_asset_code.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_success.json"));

        accountActivator.activate(account);

        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
    }

    @Test
    public void activate_HasTrustAlready_NoChangeTrust() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_account.json"));

        accountActivator.activate(account);

        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.getRequestCount(), equalTo(1));
    }

    @Test
    public void activate_AccountNotExist() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404));

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_FROM)));

        accountActivator.activate(account);
    }

    @Test
    public void activate_GetAccountQuery_HttpResponseError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        try {
            accountActivator.activate(account);
            fail("Expected OperationFailedException");
        } catch (Exception ex) {
            Assert.assertThat(ex, is(instanceOf(OperationFailedException.class)));
            Assert.assertThat(ex.getCause(), is(instanceOf(HttpResponseException.class)));
            Assert.assertThat(((HttpResponseException) ex.getCause()).getStatusCode(), is(500));
        }
    }

    @Test
    public void activate_GeneralStellarError() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_account_no_kin.json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_general_stellar_error.json"))
            .setResponseCode(400)
        );

        expectedEx.expect(TransactionFailedException.class);
        expectedEx.expect(new HasPropertyWithValue<>("transactionResultCode", equalTo("tx_failed")));
        expectedEx.expect(new HasPropertyWithValue<>("operationsResultCodes", contains("op_malformed")));
        expectedEx.expect(new HasPropertyWithValue<>("operationsResultCodes", hasSize(1)));

        accountActivator.activate(account);
    }

    @Test
    public void activate_GetAccountQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        accountActivator.activate(account);
    }

    @Test
    public void activate_TransactionQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_account_no_kin.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);

        accountActivator.activate(account);
    }

    @Test(timeout = 500)
    public void activate_ChangeTimeOut() throws Exception {
        String url = mockWebServer.url("").toString();
        server = new Server(url, 100, TimeUnit.MILLISECONDS);
        KinAsset kinAsset = createKinAsset(ACCOUNT_ID_KIN_ISSUER);
        accountActivator = new AccountActivator(server, kinAsset);

        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_account_no_kin.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(SocketTimeoutException.class));
        accountActivator.activate(account);
    }

    @Test
    public void activate_GetAccountQuery_NullResponse() throws Exception {
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID_FROM);

        accountActivator.activate(account);
    }

    @Test
    public void activate_SendTransactionQuery_NullResponse() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "activate_account_no_kin.json"));
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage("transaction");

        accountActivator.activate(account);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void activate_NullAccount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("account");
        accountActivator.activate(null);
    }

}