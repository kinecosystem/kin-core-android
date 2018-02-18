package kin.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import kin.core.exception.AccountDeletedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;

public class KinAccountImplTest {

    private static final String PASSPHRASE = "123456";
    @Mock
    private TransactionSender mockTransactionSender;
    @Mock
    private BalanceQuery mockBalanceQuery;
    @Mock
    private AccountActivator mockAccountActivator;
    private KinAccountImpl kinAccount;
    private Account expectedRandomAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private void initWithRandomAccount() {
        KeyPair keyPair = KeyPair.random();
        expectedRandomAccount = new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
        kinAccount = new KinAccountImpl(expectedRandomAccount, mockTransactionSender, mockAccountActivator,
            mockBalanceQuery);
    }

    @Test
    public void getPublicAddress_ExistingAccount() throws Exception {
        initWithRandomAccount();

        assertEquals(expectedRandomAccount.getAccountId(), kinAccount.getPublicAddress());
    }

    @Test
    public void sendTransactionSync() throws Exception {
        initWithRandomAccount();

        String expectedPassphrase = PASSPHRASE;
        String expectedAccountId = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
        BigDecimal expectedAmount = new BigDecimal("12.2");
        TransactionId expectedTransactionId = new TransactionIdImpl("myId");

        when(mockTransactionSender.sendTransaction((Account) any(), (String) any(), (String) any(), (BigDecimal) any()))
            .thenReturn(expectedTransactionId);

        TransactionId transactionId = kinAccount
            .sendTransactionSync(expectedAccountId, expectedPassphrase, expectedAmount);

        verify(mockTransactionSender)
            .sendTransaction(expectedRandomAccount, expectedPassphrase, expectedAccountId, expectedAmount);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void sendTransactionSync_WithMemo() throws Exception {
        initWithRandomAccount();

        String expectedPassphrase = PASSPHRASE;
        String expectedAccountId = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
        BigDecimal expectedAmount = new BigDecimal("12.2");
        TransactionId expectedTransactionId = new TransactionIdImpl("myId");
        byte[] memo = "Dummy Memo".getBytes();

        when(mockTransactionSender
            .sendTransaction((Account) any(), anyString(), (String) any(), (BigDecimal) any(), (byte[]) any()))
            .thenReturn(expectedTransactionId);

        TransactionId transactionId = kinAccount
            .sendTransactionSync(expectedAccountId, expectedPassphrase, expectedAmount, memo);

        verify(mockTransactionSender)
            .sendTransaction(expectedRandomAccount, expectedPassphrase, expectedAccountId, expectedAmount, memo);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void getBalanceSync() throws Exception {
        initWithRandomAccount();

        Balance expectedBalance = new BalanceImpl(new BigDecimal("11.0"));
        when(mockBalanceQuery.getBalance((Account) any())).thenReturn(expectedBalance);

        Balance balance = kinAccount.getBalanceSync();

        assertEquals(expectedBalance, balance);
        verify(mockBalanceQuery).getBalance(expectedRandomAccount);
    }

    @Test
    public void activateSync() throws Exception {
        initWithRandomAccount();
        String expectedPassphrase = PASSPHRASE;

        kinAccount.activateSync(expectedPassphrase);

        verify(mockAccountActivator).activate(expectedRandomAccount, expectedPassphrase);
    }

    @Test(expected = AccountDeletedException.class)
    public void sendTransactionSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        kinAccount.sendTransactionSync("GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX", PASSPHRASE,
            new BigDecimal("12.2"));
    }

    @Test(expected = AccountDeletedException.class)
    public void getBalanceSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();

        kinAccount.markAsDeleted();
        kinAccount.getBalanceSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void activateSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();

        kinAccount.markAsDeleted();
        kinAccount.activateSync(PASSPHRASE);
    }

    @Test
    public void getPublicAddress_DeletedAccount_Empty() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        assertNull(kinAccount.getPublicAddress());
    }
}