package kin.sdk.core;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import android.support.test.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("deprecation")
public class KinClientIntegrationTest {

    private static final String TEST_NETWORK_URL = "https://horizon-testnet.stellar.org";
    private static final String PASSPHRASE = "12345678";
    private ServiceProvider serviceProvider;
    private KinClient kinClient;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        serviceProvider = new ServiceProvider(TEST_NETWORK_URL, ServiceProvider.NETWORK_ID_TEST);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);
        kinClient.wipeoutAccount();
    }

    @Test
    public void addAccount_NewAccount() throws Exception {
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
        assertThat(kinAccount.getPublicAddress(), not(equalTo(kinAccount2.getPublicAddress())));
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
        assertThat(kinAccount.getPublicAddress(), equalTo(expectedAccount1.getPublicAddress()));
        assertThat(kinAccount2.getPublicAddress(), equalTo(expectedAccount2.getPublicAddress()));
    }

    @Test
    public void getAccount_ExistingAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);
        KinAccount kinAccount2 = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccount3 = kinClient.addAccount(PASSPHRASE);

        KinAccount expectedAccount3 = kinClient.getAccount(2);
        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertNotNull(expectedAccount3);
        assertThat(kinAccount1.getPublicAddress(), equalTo(expectedAccount1.getPublicAddress()));
        assertThat(kinAccount2.getPublicAddress(), equalTo(expectedAccount2.getPublicAddress()));
        assertThat(kinAccount3.getPublicAddress(), equalTo(expectedAccount3.getPublicAddress()));
    }

    @Test
    public void getAccount_ExistingMultipleAccount() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount(PASSPHRASE);
        KinAccount kinAccount2 = kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);
        KinAccount expectedAccount2 = kinClient.getAccount(1);
        KinAccount expectedAccount1 = kinClient.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertThat(kinAccount1.getPublicAddress(), equalTo(expectedAccount1.getPublicAddress()));
        assertThat(kinAccount2.getPublicAddress(), equalTo(expectedAccount2.getPublicAddress()));
    }


    @Test
    public void getAccount_NegativeIndex() throws Exception {
        kinClient.addAccount(PASSPHRASE);

        assertNull(kinClient.getAccount(-1));
    }

    @Test
    public void addAccount_ExistingAccount_SameAccount() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        KinAccount kinAccount = kinClient.addAccount(PASSPHRASE);

        assertEquals(kinAccount1.getPublicAddress(), kinAccount.getPublicAddress());
    }

    @Test
    public void getAccount_EmptyKeyStore_Null() throws Exception {
        KinAccount kinAccount = kinClient.getAccount(0);

        assertNull(kinAccount);
    }

    @Test
    public void getAccount_ExistingAccount_SameAccount() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        KinAccount kinAccount = kinClient.getAccount(0);

        assertEquals(kinAccount1.getPublicAddress(), kinAccount.getPublicAddress());
    }


    @Test
    public void hasAccount_EmptyKeyStore_False() throws Exception {
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingAccount_True() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingMultipleAccounts_True() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        assertTrue(kinClient.hasAccount());
        kinClient.deleteAccount(0, PASSPHRASE);
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount_MultipleAccounts() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        kinClient.deleteAccount(0, PASSPHRASE);

        assertTrue(kinClient.hasAccount());
        kinClient.deleteAccount(0, PASSPHRASE);
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount_AtIndex() throws Exception {
        KinAccount kinAccount1 = kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        kinClient.deleteAccount(1, PASSPHRASE);

        assertTrue(kinClient.hasAccount());
        assertThat(kinAccount1.getPublicAddress(), equalTo(kinClient.getAccount(0).getPublicAddress()));
    }

    @Test
    public void deleteAccount_IndexOutOfBounds() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);

        kinClient.deleteAccount(3, PASSPHRASE);

        assertNotNull(kinClient.getAccount(0));
        assertNotNull(kinClient.getAccount(1));
        assertThat(kinClient.getAccountCount(), equalTo(2));
    }

    @Test
    public void deleteAccount_NegativeIndex() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);

        kinClient.deleteAccount(-1, PASSPHRASE);

        assertNotNull(kinClient.getAccount(0));
        assertNotNull(kinClient.getAccount(1));
        assertThat(kinClient.getAccountCount(), equalTo(2));
    }

    @Test
    public void getAccountCount() throws Exception {
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

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
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient.addAccount(PASSPHRASE);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);

        kinClient.wipeoutAccount();

        assertThat(kinClient.getAccountCount(), equalTo(0));
    }

    @Test
    public void getServiceProvider() throws Exception {
        String url = "https://www.myawesomeserver.com";
        ServiceProvider serviceProvider = new ServiceProvider(url, ServiceProvider.NETWORK_ID_TEST);
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider);
        ServiceProvider actualServiceProvider = kinClient.getServiceProvider();

        assertNotNull(actualServiceProvider);
        assertFalse(actualServiceProvider.isMainNet());
        assertEquals(url, actualServiceProvider.getProviderUrl());
        assertEquals(ServiceProvider.NETWORK_ID_TEST, actualServiceProvider.getNetworkId());
    }

}
