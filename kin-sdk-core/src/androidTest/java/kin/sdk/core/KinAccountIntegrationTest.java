package kin.sdk.core;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.AccountNotActivatedException;
import kin.sdk.core.exception.AccountNotFoundException;
import kin.sdk.core.exception.TransactionFailedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("deprecation")
public class KinAccountIntegrationTest {

    private static final String TEST_NETWORK_URL = "https://horizon-testnet.stellar.org";
    private static final String PASSPHRASE = "12345678";
    private static FakeKinIssuer fakeKinIssuer;
    private KinClient kinClient;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @BeforeClass
    static public void setupKinIssuer() throws IOException {
        fakeKinIssuer = new FakeKinIssuer();
    }

    @Before
    public void setup() throws IOException {
        ServiceProvider serviceProvider = new ServiceProvider(TEST_NETWORK_URL, FakeKinIssuer.KIN_ISSUER_ACCOUNT_ID);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);
        kinClient.wipeoutAccount();
    }

    @Test
    @LargeTest
    public void getBalanceSync_AccountNotCreated_AccountNotFoundException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expectMessage(kinAccount.getPublicAddress());
        kinAccount.getBalanceSync();
    }

    @Test
    @LargeTest
    public void getBalanceSync_AccountNotActivated_AccountNotActivatedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccount.getPublicAddress());

        expectedEx.expect(AccountNotActivatedException.class);
        expectedEx.expectMessage(kinAccount.getPublicAddress());
        kinAccount.getBalanceSync();
    }

    @Test
    @LargeTest
    public void getBalanceSync_FundedAccount_GotBalance() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccount.getPublicAddress());

        kinAccount.activateSync(PASSPHRASE);
        assertThat(kinAccount.getBalanceSync().value(), equalTo(new BigDecimal("0.0000000")));

        fakeKinIssuer.fundWithKin(kinAccount.getPublicAddress(), "3.1415926");
        assertThat(kinAccount.getBalanceSync().value(), equalTo(new BigDecimal("3.1415926")));
    }

    @Test
    @LargeTest
    public void activateSync_AccountNotCreated_AccountNotFoundException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expectMessage(kinAccount.getPublicAddress());
        kinAccount.activateSync(PASSPHRASE);
    }

    @Test
    @LargeTest
    public void sendTransaction() throws Exception {
        KinAccount kinAccountSender = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccountReceiver = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccountSender.getPublicAddress());
        fakeKinIssuer.createAccount(kinAccountReceiver.getPublicAddress());

        kinAccountSender.activateSync(PASSPHRASE);
        kinAccountReceiver.activateSync(PASSPHRASE);
        fakeKinIssuer.fundWithKin(kinAccountSender.getPublicAddress(), "100");

        kinAccountSender
            .sendTransactionSync(kinAccountReceiver.getPublicAddress(), PASSPHRASE, new BigDecimal("21.123"));
        assertThat(kinAccountSender.getBalanceSync().value(), equalTo(new BigDecimal("78.8770000")));
        assertThat(kinAccountReceiver.getBalanceSync().value(), equalTo(new BigDecimal("21.1230000")));
    }

    @Test
    @LargeTest
    public void sendTransaction_ReceiverAccountNotCreated_AccountNotFoundException() throws Exception {
        KinAccount kinAccountSender = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccountReceiver = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccountSender.getPublicAddress());

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expectMessage(kinAccountReceiver.getPublicAddress());
        kinAccountSender
            .sendTransactionSync(kinAccountReceiver.getPublicAddress(), PASSPHRASE, new BigDecimal("21.123"));
    }

    @Test
    @LargeTest
    public void sendTransaction_SenderAccountNotCreated_AccountNotFoundException() throws Exception {
        KinAccount kinAccountSender = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccountReceiver = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccountReceiver.getPublicAddress());
        kinAccountReceiver.activateSync(PASSPHRASE);

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expectMessage(kinAccountSender.getPublicAddress());
        kinAccountSender
            .sendTransactionSync(kinAccountReceiver.getPublicAddress(), PASSPHRASE, new BigDecimal("21.123"));
    }

    @Test
    @LargeTest
    public void sendTransaction_ReceiverAccountNotActivated_AccountNotFoundException() throws Exception {
        KinAccount kinAccountSender = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccountReceiver = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccountSender.getPublicAddress());
        fakeKinIssuer.createAccount(kinAccountReceiver.getPublicAddress());

        kinAccountSender.activateSync(PASSPHRASE);

        expectedEx.expect(AccountNotActivatedException.class);
        expectedEx.expectMessage(kinAccountReceiver.getPublicAddress());
        kinAccountSender
            .sendTransactionSync(kinAccountReceiver.getPublicAddress(), PASSPHRASE, new BigDecimal("21.123"));
    }

    @Test
    @LargeTest
    public void sendTransaction_SenderAccountNotActivated_AccountNotFoundException() throws Exception {
        KinAccount kinAccountSender = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccountReceiver = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccountSender.getPublicAddress());
        fakeKinIssuer.createAccount(kinAccountReceiver.getPublicAddress());

        kinAccountReceiver.activateSync(PASSPHRASE);

        expectedEx.expect(AccountNotActivatedException.class);
        expectedEx.expectMessage(kinAccountSender.getPublicAddress());
        kinAccountSender
            .sendTransactionSync(kinAccountReceiver.getPublicAddress(), PASSPHRASE, new BigDecimal("21.123"));
    }

    @Test
    @LargeTest
    public void sendTransaction_NotEnoughKin_TransactionFailedException() throws Exception {
        KinAccount kinAccountSender = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccountReceiver = kinClient.addAccount(PASSPHRASE);
        fakeKinIssuer.createAccount(kinAccountSender.getPublicAddress());
        fakeKinIssuer.createAccount(kinAccountReceiver.getPublicAddress());

        kinAccountSender.activateSync(PASSPHRASE);
        kinAccountReceiver.activateSync(PASSPHRASE);

        expectedEx.expect(TransactionFailedException.class);
        expectedEx.expectMessage("underfunded");
        kinAccountSender
            .sendTransactionSync(kinAccountReceiver.getPublicAddress(), PASSPHRASE, new BigDecimal("21.123"));
    }

    @Test(expected = AccountDeletedException.class)
    public void activateSync_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        kinClient.deleteAccount(PASSPHRASE);
        kinAccount.activateSync(PASSPHRASE);
    }

    @Test(expected = AccountDeletedException.class)
    public void getBalanceSync_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        kinClient.deleteAccount(PASSPHRASE);
        kinAccount.getBalanceSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void sendTransactionSync_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        kinClient.deleteAccount(PASSPHRASE);
        kinAccount.sendTransactionSync("GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM", PASSPHRASE,
            new BigDecimal(10));
    }

    @Test
    public void getPublicAddress_DeletedAccount_EmptyPublicAddress() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        kinClient.deleteAccount(PASSPHRASE);
        assertThat(kinAccount.getPublicAddress(), isEmptyString());
    }

}
