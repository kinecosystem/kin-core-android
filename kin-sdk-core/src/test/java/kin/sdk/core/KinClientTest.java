package kin.sdk.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import kin.sdk.core.exception.CreateAccountException;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stellar.sdk.KeyPair;

public class KinClientTest {

    private static final String PASSPHRASE = "123456";
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @Mock
    private ClientWrapper clientWrapper;
    private KinClient kinClient;
    private KeyStore fakeKeyStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(clientWrapper.getKeyStore()).thenAnswer(invocation -> fakeKeyStore);

        kinClient = new KinClient(clientWrapper);
    }

    @Test
    public void createAccount_NewAccount() throws Exception {
        fakeKeyStore = new FakeKeyStore();

        KinAccount kinAccount = kinClient.createAccount("123456");

        assertNotNull(kinAccount);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
    }

    @Test
    public void createAccount_Exception() throws Exception {
        String expectedExString = "My Exception";
        fakeKeyStore = mock(KeyStore.class);
        when(fakeKeyStore.newAccount(anyString())).thenThrow(new RuntimeException(expectedExString));
        expectedEx.expect(CreateAccountException.class);
        expectedEx.expectCause(new HasPropertyWithValue<>("message", is(expectedExString)));

        kinClient.createAccount("123456");
    }

    @Test
    public void createAccount_ExistingAccount_SameAccount() throws Exception {
        Account account = createKeyStoreWithRandomAccount();

        KinAccount kinAccount = kinClient.createAccount(PASSPHRASE);

        assertEquals(account.getAccountId(), kinAccount.getPublicAddress());
    }

    @NonNull
    private Account createRandomAccount() {
        KeyPair keyPair = KeyPair.random();
        return new Account(new String(keyPair.getSecretSeed()), keyPair.getAccountId());
    }

    @Test
    public void getAccount_EmptyKeyStore_Null() throws Exception {
        fakeKeyStore = new FakeKeyStore();

        KinAccount kinAccount = kinClient.getAccount();

        assertNull(kinAccount);
    }

    @Test
    public void getAccount_ExistingAccount_SameAccount() throws Exception {
        Account account = createKeyStoreWithRandomAccount();

        KinAccount kinAccount = kinClient.getAccount();

        assertEquals(account.getAccountId(), kinAccount.getPublicAddress());
    }

    @NonNull
    private Account createKeyStoreWithRandomAccount() {
        Account account = createRandomAccount();
        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(account);
        fakeKeyStore = new FakeKeyStore(accounts);
        return account;
    }

    @Test
    public void hasAccount_EmptyKeyStore_False() throws Exception {
        fakeKeyStore = new FakeKeyStore();

        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void hasAccount_ExistingAccount_True() throws Exception {
        createKeyStoreWithRandomAccount();

        assertTrue(kinClient.hasAccount());
    }

    @Test
    public void deleteAccount() throws Exception {
        createKeyStoreWithRandomAccount();

        assertTrue(kinClient.hasAccount());

        kinClient.deleteAccount(PASSPHRASE);

        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void getServiceProvider() throws Exception {
        String url = "My awesome Horizon server";
        ServiceProvider serviceProvider = new ServiceProvider(url, ServiceProvider.NETWORK_ID_TEST);
        when(clientWrapper.getServiceProvider()).thenReturn(serviceProvider);

        ServiceProvider actualServiceProvider = kinClient.getServiceProvider();
        assertNotNull(actualServiceProvider);
        assertFalse(actualServiceProvider.isMainNet());
        assertEquals(url, actualServiceProvider.getProviderUrl());
        assertEquals(ServiceProvider.NETWORK_ID_TEST, actualServiceProvider.getNetworkId());
    }

}