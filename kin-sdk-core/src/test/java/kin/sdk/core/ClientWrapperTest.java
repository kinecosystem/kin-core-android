package kin.sdk.core;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;

public class ClientWrapperTest {

    @Mock
    private TransactionSender mockTransactionSender;
    @Mock
    private AccountActivator mockAccountActivator;
    @Mock
    private BalanceQuery mockBalanceQuery;

    private ClientWrapper clientWrapper;
    private ServiceProvider fakeServiceProvider;
    private FakeKeyStore fakeKeyStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        fakeServiceProvider = new ServiceProvider("", ServiceProvider.NETWORK_ID_TEST);
        fakeKeyStore = new FakeKeyStore();

        clientWrapper = new ClientWrapper(fakeServiceProvider, fakeKeyStore, mockTransactionSender,
            mockAccountActivator, mockBalanceQuery);
    }

    @Test
    public void wipeoutAccount() throws Exception {
        //TODO
    }

    @Test
    public void getKeyStore() throws Exception {
        KeyStore actualKeyStore = clientWrapper.getKeyStore();

        assertEquals(fakeKeyStore, actualKeyStore);
    }

    @Test
    public void sendTransaction() throws Exception {
        KeyPair keyPair = KeyPair.random();
        Account expectedFrom = new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
        String expectedPassphrase = "123456";
        String expectedAccountId = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
        BigDecimal expectedAmount = new BigDecimal("12.2");
        TransactionId expectedTransactionId = new TransactionIdImpl("myId");
        when(mockTransactionSender
            .sendTransaction((Account) any(), (String) any(), (String) any(), (BigDecimal) any()))
            .thenReturn(expectedTransactionId);

        TransactionId transactionId = clientWrapper
            .sendTransaction(expectedFrom, expectedPassphrase, expectedAccountId, expectedAmount);

        verify(mockTransactionSender)
            .sendTransaction(expectedFrom, expectedPassphrase, expectedAccountId, expectedAmount);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void getBalance() throws Exception {
        KeyPair keyPair = KeyPair.random();
        Account expectedFrom = new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
        Balance expectedBalance = new BalanceImpl(new BigDecimal("11.0"));
        when(mockBalanceQuery.getBalance((Account) any())).thenReturn(expectedBalance);

        Balance balance = clientWrapper.getBalance(expectedFrom);

        assertEquals(expectedBalance, balance);
        verify(mockBalanceQuery).getBalance(expectedFrom);
    }

    @Test
    public void activateAccount() throws Exception {
        KeyPair keyPair = KeyPair.random();
        Account expectedFrom = new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
        String expectedPassphrase = "123456";

        clientWrapper.activateAccount(expectedFrom, expectedPassphrase);

        verify(mockAccountActivator).activate(expectedFrom, expectedPassphrase);
    }

    @Test
    public void getServiceProvider() throws Exception {
        ServiceProvider actualServiceProvider = clientWrapper.getServiceProvider();

        assertEquals(actualServiceProvider.isMainNet(), fakeServiceProvider.isMainNet());
        assertEquals(actualServiceProvider.getProviderUrl(), fakeServiceProvider.getProviderUrl());
    }

}