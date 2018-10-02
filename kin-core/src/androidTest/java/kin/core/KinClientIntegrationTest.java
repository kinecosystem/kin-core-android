package kin.core;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static kin.core.IntegConsts.TEST_NETWORK_ID;
import static kin.core.IntegConsts.TEST_NETWORK_URL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import android.support.test.InstrumentationRegistry;
import kin.core.exception.CreateAccountException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("deprecation")
public class KinClientIntegrationTest {

    private static final String STORE_KEY_TEST = "test";
    private static final String STORE_KEY_TEST2 = "test2";
    private Environment serviceProvider;
    private KinClient kinClient;
    private KinClient kinClient2;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        serviceProvider = new Environment.Builder()
            .networkUrl(TEST_NETWORK_URL)
            .networkPassphrase(TEST_NETWORK_ID)
            .issuerAccountId(Environment.TEST.getIssuerAccountId())
            .build();
        kinClient = createNewKinClient(STORE_KEY_TEST);
        kinClient2 = createNewKinClient(STORE_KEY_TEST2);
        kinClient.clearAllAccounts();
        kinClient2.clearAllAccounts();
    }

    private KinClient createNewKinClient(String storeKey) {
        return new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, storeKey);
    }

    @After
    public void teardown() {
        kinClient = createNewKinClient(STORE_KEY_TEST);
        kinClient2 = createNewKinClient(STORE_KEY_TEST2);
        kinClient.clearAllAccounts();
        kinClient2.clearAllAccounts();
    }

    @Test
    public void addAccount_NewAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();

        assertNotNull(kinAccount);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
    }

    @Test
    public void createAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        KinAccount kinAccount2 = kinClient.addAccount();

        assertNotNull(kinAccount);
        assertNotNull(kinAccount2);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
        assertThat(kinAccount2.getPublicAddress(), not(isEmptyOrNullString()));
        assertThat(kinAccount, not(equalTo(kinAccount2)));
    }

    @Test
    public void getAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        KinAccount kinAccount2 = kinClient.addAccount();

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
        KinAccount kinAccount1 = kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);
        KinAccount kinAccount2 = kinClient.addAccount();
        KinAccount kinAccount3 = kinClient.addAccount();

        KinAccount expectedAccount3 = kinClient.getAccount(2);
        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertNotNull(expectedAccount3);
        assertThat(kinAccount1, equalTo(expectedAccount1));
        assertThat(kinAccount2, equalTo(expectedAccount2));
        assertThat(kinAccount3, equalTo(expectedAccount3));
    }

    @Test
    public void getAccount_ExistingMultipleAccount() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount();
        KinAccount kinAccount2 = kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);
        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertThat(kinAccount1, equalTo(expectedAccount1));
        assertThat(kinAccount2, equalTo(expectedAccount2));
    }


    @Test
    public void getAccount_NegativeIndex() throws Exception {
        kinClient.addAccount();

        assertNull(kinClient.getAccount(-1));
    }

    @Test
    public void getAccount_EmptyKeyStore_Null() throws Exception {
        KinAccount kinAccount = kinClient.getAccount(0);

        assertNull(kinAccount);
    }

    @Test
    public void getAccount_ExistingAccount_SameAccount() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        KinAccount kinAccount = kinClient.getAccount(0);

        assertEquals(kinAccount1, kinAccount);
    }

    @Test
    public void hasAccount_EmptyKeyStore_False() throws Exception {
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingAccount_True() throws Exception {
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingMultipleAccounts_True() throws Exception {
        kinClient.addAccount();
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount() throws Exception {
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        assertTrue(kinClient.hasAccount());
        kinClient.deleteAccount(0);
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount_MultipleAccounts() throws Exception {
        kinClient.addAccount();
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        kinClient.deleteAccount(0);

        assertTrue(kinClient.hasAccount());
        kinClient.deleteAccount(0);
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount_AtIndex() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount();
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        kinClient.deleteAccount(1);

        assertTrue(kinClient.hasAccount());
        assertThat(kinAccount1, equalTo(kinClient.getAccount(0)));
    }

    @Test
    public void deleteAccount_IndexOutOfBounds() throws Exception {
        kinClient.addAccount();
        kinClient.addAccount();

        kinClient.deleteAccount(3);

        assertNotNull(kinClient.getAccount(0));
        assertNotNull(kinClient.getAccount(1));
        assertThat(kinClient.getAccountCount(), equalTo(2));
    }

    @Test
    public void deleteAccount_NegativeIndex() throws Exception {
        kinClient.addAccount();
        kinClient.addAccount();

        kinClient.deleteAccount(-1);

        assertNotNull(kinClient.getAccount(0));
        assertNotNull(kinClient.getAccount(1));
        assertThat(kinClient.getAccountCount(), equalTo(2));
    }

    @Test
    public void getAccountCount() throws Exception {
        kinClient.addAccount();
        kinClient.addAccount();
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        assertThat(kinClient.getAccountCount(), equalTo(3));
        kinClient.deleteAccount(2);
        kinClient.deleteAccount(1);
        assertThat(kinClient.getAccountCount(), equalTo(1));
        kinClient.addAccount();
        assertThat(kinClient.getAccountCount(), equalTo(2));
        kinClient.deleteAccount(1);
        kinClient.deleteAccount(0);
        assertThat(kinClient.getAccountCount(), equalTo(0));
    }

    @Test
    public void clearAllAccounts() throws CreateAccountException {
        kinClient.addAccount();
        kinClient.addAccount();
        kinClient.addAccount();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);

        kinClient.clearAllAccounts();

        assertThat(kinClient.getAccountCount(), equalTo(0));
    }

    @Test
    public void getServiceProvider() throws Exception {
        String url = "https://www.myawesomeserver.com";
        Environment serviceProvider = new Environment.Builder()
            .networkUrl(url)
            .networkPassphrase(Environment.TEST.getNetworkPassphrase())
            .issuerAccountId(Environment.TEST.getIssuerAccountId())
            .build();
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider, STORE_KEY_TEST);
        Environment actualServiceProvider = kinClient.getServiceProvider();

        assertNotNull(actualServiceProvider);
        assertFalse(actualServiceProvider.isMainNet());
        assertEquals(url, actualServiceProvider.getNetworkUrl());
        assertEquals(Environment.TEST.getNetworkPassphrase(), actualServiceProvider.getNetworkPassphrase());
    }

    @Test
    public void multipleKinClients_AddAccount_DifferentAccounts() throws Exception {
        KinAccount account = kinClient.addAccount();

        kinClient2 = createNewKinClient(STORE_KEY_TEST2);
        assertThat(kinClient2.getAccount(0), nullValue());

        KinAccount account2 = kinClient2.addAccount();

        kinClient = createNewKinClient(STORE_KEY_TEST);
        assertThat(kinClient.getAccount(0), equalTo(account));
        assertThat(kinClient2.getAccount(0), equalTo(account2));
        assertThat(account, not(equalTo(account2)));
    }

    @Test
    public void multipleKinClients_DeleteAccount_DifferentAccounts() throws Exception {
        KinAccount account = kinClient2.addAccount();

        kinClient.clearAllAccounts();
        kinClient2 = createNewKinClient(STORE_KEY_TEST2);

        assertThat(kinClient2.getAccount(0), equalTo(account));
    }

    @Test(expected = IllegalArgumentException.class)
    public void initKinClient_NullId_Exception() {
        kinClient = createNewKinClient(null);
    }
}
