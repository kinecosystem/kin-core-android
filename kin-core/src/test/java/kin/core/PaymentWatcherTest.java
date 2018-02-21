package kin.core;

import static kin.core.TestUtils.loadResource;
import static kin.core.TestUtils.memoHashFromString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import kin.core.ServiceProvider.KinAsset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.TransactionsRequestBuilder;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.sse.ServerSentEvent;

public class PaymentWatcherTest {

    private static final String ACCOUNT_ID = "GBRXY5BAZAAB7M2PI3KG5WLIRARJAGUPV2IPC4AGIPTTZRM7UY2VVKN3";
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @Mock
    private Server server;
    @Mock
    private TransactionsRequestBuilder mockTransactionsRequestBuilder;
    @Mock
    private ServerSentEvent mockServerSentEvent;
    private PaymentWatcher paymentWatcher;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockServer();
        Network.useTestNetwork();

        KinAsset kinAsset = new ServiceProvider("", ServiceProvider.NETWORK_ID_TEST).getKinAsset();
        Account account = new Account("", ACCOUNT_ID);
        paymentWatcher = new PaymentWatcher(server, account, kinAsset);
    }

    private void mockServer() throws IOException {
        when(server.transactions()).thenReturn(mockTransactionsRequestBuilder);
        when(mockTransactionsRequestBuilder.forAccount((KeyPair) any())).thenReturn(mockTransactionsRequestBuilder);
        when(mockTransactionsRequestBuilder.cursor(anyString())).thenReturn(mockTransactionsRequestBuilder);
        when(mockTransactionsRequestBuilder.stream(ArgumentMatchers.<EventListener<TransactionResponse>>any()))
            .then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    final EventListener<TransactionResponse> listener = invocation.getArgument(0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sleep();
                            listener.onEvent(createTransactionResponse("payment_watcher_tx_response1.json"));
                            sleep();
                            listener.onEvent(createTransactionResponse("payment_watcher_tx_response2.json"));
                        }

                        private void sleep() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    return mockServerSentEvent;
                }
            });

    }

    private TransactionResponse createTransactionResponse(String res) {
        return GsonSingleton.getInstance()
            .fromJson(loadResource(PaymentWatcherTest.this.getClass(), res),
                TransactionResponse.class);
    }

    @Test
    public void start() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<PaymentInfo> actualResults = new ArrayList<>();
        paymentWatcher.start(new WatcherListener<PaymentInfo>() {
            @Override
            public void onEvent(PaymentInfo data) {
                actualResults.add(data);
                if (actualResults.size() == 2) {
                    latch.countDown();
                }
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        assertThat(actualResults.size(), equalTo(2));
        PaymentInfo payment1 = actualResults.get(0);
        PaymentInfo payment2 = actualResults.get(1);
        assertThat(payment1.hash().id(), equalTo("13a3eeceb2ef63223b89e179582b4f4a6ce3fdb310bdb19454847a14f9570be8"));
        assertThat(payment1.sourcePublicKey(), equalTo("GBRXY5BAZAAB7M2PI3KG5WLIRARJAGUPV2IPC4AGIPTTZRM7UY2VVKN3"));
        assertThat(payment1.destinationPublicKey(),
            equalTo("GD4YOKVYR6KPPXA7HXG2SQOTWGZ6FO6BNCDJ5IGIGWRLL3Z5ABPEEYD3"));
        assertThat(payment1.amount(), equalTo(new BigDecimal("612.784")));
        assertThat(payment1.createdAt(), equalTo("2018-02-21T06:51:00Z"));
        assertThat(payment1.memo(), equalTo(memoHashFromString("Test Transaction")));

        assertThat(payment2.hash().id(), equalTo("899a639e280d91c917e82816c803e4ec68025a6352fd2b3769403012a4ee3cb4"));
        assertThat(payment2.sourcePublicKey(), equalTo("GBRXY5BAZAAB7M2PI3KG5WLIRARJAGUPV2IPC4AGIPTTZRM7UY2VVKN3"));
        assertThat(payment2.destinationPublicKey(),
            equalTo("GD4YOKVYR6KPPXA7HXG2SQOTWGZ6FO6BNCDJ5IGIGWRLL3Z5ABPEEYD3"));
        assertThat(payment2.amount(), equalTo(new BigDecimal("147.32564")));
        assertThat(payment2.createdAt(), equalTo("2018-02-21T06:51:24Z"));
        assertThat(payment2.memo(), equalTo(memoHashFromString("Test Transaction")));
    }

    @Test
    public void start_AlreadyStarted_IllegalStateException() throws Exception {
        paymentWatcher.start(new WatcherListener<PaymentInfo>() {
            @Override
            public void onEvent(PaymentInfo data) {
            }
        });
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("started");
        paymentWatcher.start(new WatcherListener<PaymentInfo>() {
            @Override
            public void onEvent(PaymentInfo data) {

            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void start_NullListener_IllegalArgumentException() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("listener");
        paymentWatcher.start(null);
    }

}