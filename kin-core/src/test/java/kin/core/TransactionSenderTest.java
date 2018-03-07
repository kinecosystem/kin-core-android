package kin.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static kin.core.TestUtils.createKinAsset;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import kin.core.ServiceProvider.KinAsset;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.InsufficientKinException;
import kin.core.exception.OperationFailedException;
import kin.core.exception.TransactionFailedException;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.stellar.sdk.FormatException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.HttpResponseException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class TransactionSenderTest {

    private static final String ACCOUNT_ID_KIN_ISSUER = "GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM";
    private static final String ACCOUNT_ID_FROM = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
    private static final String SECRET_SEED_FROM = "SB6PCLT2WUQF44HVOTEGCXIDYNX2U4BJUPWUX453ODRGD4CXGPJP3HUX";
    private static final String ACCOUNT_ID_TO = "GDJOJJVIWI6YVPUI3PX4BQCC4SQUZTRYIAMV2YBT6QVL54QGQUQSFKGM";
    private static final String SECRET_SEED_TO = "SCJFLXKUY6VQT2LYSP6XDP23WNEP5OITSC3LZEJUJO7GFZM7QLDF2BCN";
    private static final String TX_BODY = "tx=AAAAANSQMFM2TD8pn4hIhHoUwA8IUMSN1M2SRw31SjZtBVodAAAAZABpZ8AAAAAEAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAA0uSmqLI9ir6I2%2B%2FAwELkoUzOOEAZXWAz9Cq%2B8gaFISIAAAABS0lOAAAAAABBq58xoA5F8Hm%2F7tPH51hBTD4tUsenooq1dLrUnnJnxgAAAAAA5OHAAAAAAAAAAAFtBVodAAAAQLLn6OJYeSG1KEki6SL%2FKYPX01Dzdid5aTNTMYTJ%2FO7cMQC1n%2FAWSmyVXJdm5zQCtn9vAzTVZpIbBmKKyHjtfw4%3D";
    private static final String TX_BODY_WITH_MEMO = "tx=AAAAANSQMFM2TD8pn4hIhHoUwA8IUMSN1M2SRw31SjZtBVodAAAAZABpZ8AAAAAEAAAAAAAAAAEAAAAJRmFrZSBNZW1vAAAAAAAAAQAAAAAAAAABAAAAANLkpqiyPYq%2BiNvvwMBC5KFMzjhAGV1gM%2FQqvvIGhSEiAAAAAUtJTgAAAAAAQaufMaAORfB5v%2B7Tx%2BdYQUw%2BLVLHp6KKtXS61J5yZ8YAAAAAAOThwAAAAAAAAAABbQVaHQAAAEBg5FzUJmYLcqxR24yo8RI6CnvJ1vDNWAHALT4XEqIR4nzrh5fqQxVnNMCP2wawlYUw46Ff0Jb%2BS4mrnUg7vzwE";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private KeyStore mockKeyStore;
    private Server server;
    private MockWebServer mockWebServer;
    private TransactionSender transactionSender;
    private Account account;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockServer();
        mockKeyStoreResponse();
        Network.useTestNetwork();

        KinAsset kinAsset = createKinAsset(ACCOUNT_ID_KIN_ISSUER);
        transactionSender = new TransactionSender(server, mockKeyStore, kinAsset);
        account = new Account(SECRET_SEED_FROM, ACCOUNT_ID_FROM);
    }

    private void mockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("").toString();
        server = new Server(url);
    }

    private void mockKeyStoreResponse() throws CryptoException {
        when(mockKeyStore.decryptAccount(any(Account.class)))
            .thenAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return KeyPair.fromSecretSeed(((Account) invocation.getArguments()[0]).getEncryptedSeed());
                    }
                });
    }

    @Test
    public void sendTransaction_success() throws Exception {
        //send transaction fetch first to account details, then from account details, and finally perform tx,
        //here we mock all 3 responses from server to achieve success operation
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_success_res.json"));

        TransactionId transactionId = transactionSender
            .sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"));

        assertEquals("8f1e0cd1d922f4c57cc1898ececcf47375e52ec4abf77a7e32d0d9bb4edecb69", transactionId.id());

        //verify sent requests data
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_TO));
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
    }

    @Test
    public void sendTransaction_WithMemo_success() throws Exception {
        //send transaction fetch first to account details, then from account details, and finally perform tx,
        //here we mock all 3 responses from server to achieve success operation
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_success_res.json"));
        String fakeMemo = "Fake Memo";

        TransactionId transactionId = transactionSender
            .sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"), fakeMemo);

        assertEquals("8f1e0cd1d922f4c57cc1898ececcf47375e52ec4abf77a7e32d0d9bb4edecb69", transactionId.id());

        //verify sent requests data
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_TO));
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY_WITH_MEMO));
    }

    @Test
    public void sendTransaction_ToAccountNotExist() throws Exception {

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404));

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_TO)));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"));
    }

    @Test
    public void sendTransaction_NoKinTrustToAccount() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to_no_kin.json"));

        expectedEx.expect(AccountNotActivatedException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_TO)));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"));
    }

    @Test
    public void sendTransaction_FromAccountNotExist() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404));

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_FROM)));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"));
    }

    @Test
    public void sendTransaction_NoKinTrustFromAccount() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from_no_kin.json"));

        expectedEx.expect(AccountNotActivatedException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_FROM)));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"));
    }

    @Test
    public void sendTransaction_UnderfundStellarError() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_internal_error.json"))
            .setResponseCode(400)
        );

        expectedEx.expect(TransactionFailedException.class);
        expectedEx.expect(new HasPropertyWithValue<>("transactionResultCode", equalTo("tx_failed")));
        expectedEx.expect(new HasPropertyWithValue<>("operationsResultCodes", contains("op_malformed")));
        expectedEx.expect(new HasPropertyWithValue<>("operationsResultCodes", hasSize(1)));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_Underfunded_InsufficientKinException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_underfunded.json"))
            .setResponseCode(400)
        );

        expectedEx.expect(InsufficientKinException.class);

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_FirstQuery_HttpResponseError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        testHttpResponseCode(500);
    }

    @Test
    public void sendTransaction_SecondQuery_HttpResponseError() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
        );

        testHttpResponseCode(500);
    }

    @Test
    public void sendTransaction_FirstQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_SecondQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_ThirdQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test(timeout = 500)
    public void sendTransaction_changeTimeOut() throws Exception {
        String url = mockWebServer.url("").toString();
        server = new Server(url, 100, TimeUnit.MILLISECONDS);
        KinAsset kinAsset = createKinAsset(ACCOUNT_ID_KIN_ISSUER);
        transactionSender = new TransactionSender(server, mockKeyStore, kinAsset);

        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(SocketTimeoutException.class));
        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_FirstQuery_NullResponse() throws Exception {
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID_TO);

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_SecondQuery_NullResponse() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID_FROM);

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_ThirdQuery_NullResponse() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage("transaction");

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_CryptoException_OperationFailedException() throws Exception {
        when(mockKeyStore.decryptAccount((Account) any())).thenThrow(CryptoException.class);
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(CryptoException.class));

        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_TooLongMemo() throws Exception {
        String tooLongMemo = "memo string can be only 28 characters";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("28 characters");
        transactionSender.sendTransaction(account, ACCOUNT_ID_FROM, new BigDecimal("200"), tooLongMemo);
        assertThat(mockWebServer.getRequestCount(), equalTo(0));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_NullAccount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("account");
        transactionSender.sendTransaction(null, ACCOUNT_ID_TO, new BigDecimal("200"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_NullPublicAddress() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("public address");
        transactionSender.sendTransaction(account, null, new BigDecimal("200"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_NullAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount");
        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, null);
    }

    @Test
    public void sendTransaction_EmptyPublicAddress() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("public address");
        transactionSender.sendTransaction(account, "", new BigDecimal("200"));
    }

    @Test
    public void sendTransaction_NegativeAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Amount");
        transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("-200"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_InvalidPublicIdLength() throws Exception {
        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(FormatException.class));
        expectedEx.expectMessage("public address");
        transactionSender.sendTransaction(account, "ABCDEF", new BigDecimal("200"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_InvalidChecksumPublicId() throws Exception {
        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(FormatException.class));
        expectedEx.expectMessage("public address");
        transactionSender.sendTransaction(account, "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OG3",
            new BigDecimal("200"));
    }

    @SuppressWarnings("SameParameterValue")
    private void testHttpResponseCode(int resCode) {
        try {
            transactionSender.sendTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"));
            fail("Expected OperationFailedException");
        } catch (Exception ex) {
            Assert.assertThat(ex, is(instanceOf(OperationFailedException.class)));
            Assert.assertThat(ex.getCause(), is(instanceOf(HttpResponseException.class)));
            Assert.assertThat(((HttpResponseException) ex.getCause()).getStatusCode(), is(resCode));
        }
    }

}