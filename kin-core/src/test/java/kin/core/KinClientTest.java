package kin.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;

@SuppressWarnings("deprecation")
public class KinClientTest {

    private static final String PASSPHRASE = "123456";
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @Mock
    private TransactionSender mockTransactionSender;
    @Mock
    private AccountActivator mockAccountActivator;
    @Mock
    private BalanceQuery mockBalanceQuery;
    @Mock
    private PaymentWatcherCreator mockPaymentWatcherCreator;
    private KinClient kinClient;
    private KeyStore fakeKeyStore;
    private ServiceProvider fakeServiceProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        fakeServiceProvider = new ServiceProvider("", ServiceProvider.NETWORK_ID_TEST);
        fakeKeyStore = new FakeKeyStore();
        kinClient = createNewKinClient();
    }

    @Test
    public void addAccount_NewAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);

        assertNotNull(kinAccount);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
    }

    @Test
    public void createAccount_AddAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);

        assertNotNull(kinAccount);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
    }

    @Test
    public void createAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccount2 = kinClient.addAccount(PASSPHRASE);

        assertNotNull(kinAccount);
        assertNotNull(kinAccount2);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
        assertThat(kinAccount2.getPublicAddress(), not(isEmptyOrNullString()));
        assertThat(kinAccount, not(equalTo(kinAccount2)));
    }

    @Test
    public void getAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccount2 = kinClient.addAccount(PASSPHRASE);

        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(kinAccount);
        assertNotNull(kinAccount2);
        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertThat(kinAccount, equalTo(expectedAccount1));
        assertThat(kinAccount2, equalTo(expectedAccount2));
    }

    @Test
    public void getAccount_ExistingAccount_AddMultipleAccount() throws Exception {
        Account account1 = createKeyStoreWithRandomAccount();

        kinClient = createNewKinClient();

        KinAccount kinAccount2 = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccount3 = kinClient.addAccount(PASSPHRASE);

        KinAccount expectedAccount3 = kinClient.getAccount(2);
        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertNotNull(expectedAccount3);
        assertThat(account1.getAccountId(), equalTo(expectedAccount1.getPublicAddress()));
        assertThat(kinAccount2, equalTo(expectedAccount2));
        assertThat(kinAccount3, equalTo(expectedAccount3));
    }

    @Test
    public void getAccount_ExistingMultipleAccount() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2));
        kinClient = createNewKinClient();
        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertThat(account1.getAccountId(), equalTo(expectedAccount1.getPublicAddress()));
        assertThat(account2.getAccountId(), equalTo(expectedAccount2.getPublicAddress()));
    }

    @Test
    public void getAccount_NegativeIndex() throws Exception {
        createKeyStoreWithRandomAccount();

        assertNull(kinClient.getAccount(-1));
    }

    @NonNull
    private Account createRandomAccount() {
        KeyPair keyPair = KeyPair.random();
        return new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
    }

    @Test
    public void getAccount_EmptyKeyStore_Null() throws Exception {
        KinAccount kinAccount = kinClient.getAccount(0);

        assertNull(kinAccount);
    }

    @Test
    public void getAccount_ExistingAccount_SameAccount() throws Exception {
        Account account = createKeyStoreWithRandomAccount();

        KinAccount kinAccount = kinClient.getAccount(0);

        assertEquals(account.getAccountId(), kinAccount.getPublicAddress());
    }

    @NonNull
    private Account createKeyStoreWithRandomAccount() {
        Account account = createRandomAccount();
        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(account);
        fakeKeyStore = new FakeKeyStore(accounts);
        kinClient = createNewKinClient();
        return account;
    }

    @Test
    public void hasAccount_EmptyKeyStore_False() throws Exception {
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingAccount_True() throws Exception {
        createKeyStoreWithRandomAccount();

        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingMultipleAccounts_True() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2));

        kinClient = createNewKinClient();
        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount() throws Exception {
        createKeyStoreWithRandomAccount();

        assertTrue(kinClient.hasAccount());
        kinClient.deleteAccount(0, PASSPHRASE);

        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount_MultipleAccounts() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2));

        kinClient = createNewKinClient();
        kinClient.deleteAccount(0, PASSPHRASE);

        assertTrue(kinClient.hasAccount());
        kinClient.deleteAccount(0, PASSPHRASE);
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount_AtIndex() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2));

        kinClient = createNewKinClient();
        kinClient.deleteAccount(1, PASSPHRASE);

        assertTrue(kinClient.hasAccount());
        assertThat(account1.getAccountId(), equalTo(kinClient.getAccount(0).getPublicAddress()));
    }

    @Test
    public void deleteAccount_IndexOutOfBounds() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2));

        kinClient = createNewKinClient();
        kinClient.deleteAccount(3, PASSPHRASE);

        assertNotNull(kinClient.getAccount(0));
        assertNotNull(kinClient.getAccount(1));
        assertThat(kinClient.getAccountCount(), equalTo(2));
    }

    @Test
    public void deleteAccount_NegativeIndex() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2));

        kinClient = createNewKinClient();
        kinClient.deleteAccount(-1, PASSPHRASE);

        assertNotNull(kinClient.getAccount(0));
        assertNotNull(kinClient.getAccount(1));
        assertThat(kinClient.getAccountCount(), equalTo(2));
    }

    @Test
    public void getAccountCount() throws Exception {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();
        Account account3 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2, account3));
        kinClient = createNewKinClient();

        assertThat(kinClient.getAccountCount(), equalTo(3));
        kinClient.deleteAccount(2, PASSPHRASE);
        kinClient.deleteAccount(1, PASSPHRASE);
        assertThat(kinClient.getAccountCount(), equalTo(1));
        kinClient.addAccount(PASSPHRASE);
        assertThat(kinClient.getAccountCount(), equalTo(2));
        kinClient.deleteAccount(1, PASSPHRASE);
        kinClient.deleteAccount(0, PASSPHRASE);
        assertThat(kinClient.getAccountCount(), equalTo(0));
    }

    @Test
    public void wipeout() {
        Account account1 = createRandomAccount();
        Account account2 = createRandomAccount();
        Account account3 = createRandomAccount();

        fakeKeyStore = new FakeKeyStore(Arrays.asList(account1, account2, account3));
        kinClient = createNewKinClient();

        kinClient.wipeoutAccount();

        assertThat(kinClient.getAccountCount(), equalTo(0));
    }

    @Test
    public void getServiceProvider() throws Exception {
        String url = "My awesome Horizon server";
        ServiceProvider serviceProvider = new ServiceProvider(url, ServiceProvider.NETWORK_ID_TEST);
        kinClient = new KinClient(serviceProvider, fakeKeyStore, mockTransactionSender, mockAccountActivator,
            mockBalanceQuery, mockPaymentWatcherCreator);
        ServiceProvider actualServiceProvider = kinClient.getServiceProvider();

        assertNotNull(actualServiceProvider);
        assertFalse(actualServiceProvider.isMainNet());
        assertEquals(url, actualServiceProvider.getProviderUrl());
        assertEquals(ServiceProvider.NETWORK_ID_TEST, actualServiceProvider.getNetworkId());
    }

    @NonNull
    private KinClient createNewKinClient() {
        return new KinClient(fakeServiceProvider, fakeKeyStore, mockTransactionSender, mockAccountActivator,
            mockBalanceQuery, mockPaymentWatcherCreator);
    }
}