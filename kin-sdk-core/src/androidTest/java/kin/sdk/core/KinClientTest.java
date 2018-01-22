package kin.sdk.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import kin.sdk.core.exception.EthereumClientException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class KinClientTest extends BaseTest {

    private static final String PASSPHRASE = "testPassphrase";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testWrongServiceProvider() throws Exception {
        expectedEx.expect(EthereumClientException.class);
        expectedEx.expectMessage("provider - could not establish connection to the provider");
        Context context = InstrumentationRegistry.getContext();
        ServiceProvider wrongProvider = new ServiceProvider("wrongProvider", 12);
        kinClient = new KinClient(context, wrongProvider);
    }

    @Test
    public void testCreateAccount() throws Exception {
        // Create first account.
        KinAccount kinAccount = createAccount();
        assertNotNull(kinAccount);
    }

    @Test
    public void testCreateSecondAccount() throws Exception {
        // Create account on the first time - should be ok
        KinAccount firstAccount = createAccount();

        // Try to create second account
        // should return the same account (firstAccount).
        KinAccount secondAccount = createAccount();

        assertEquals(firstAccount, secondAccount);
    }

    @Test
    public void testDeleteAccount() throws Exception {
        createAccount();
        assertTrue(kinClient.hasAccount());

        kinClient.deleteAccount(PASSPHRASE);
        assertFalse(kinClient.hasAccount());
        assertNull(kinClient.getAccount());
    }

    @Test
    public void testWipeAccount() throws Exception {
        createAccount();
        assertTrue(kinClient.hasAccount());

        kinClient.wipeoutAccount();
        assertFalse(kinClient.hasAccount());
        assertNull(kinClient.getAccount());
    }

    @Test
    public void testGetAccount_isNull() throws Exception {
        // No account were created, thus the account is null
        KinAccount kinAccount = kinClient.getAccount();
        assertNull(kinAccount);
    }

    @Test
    public void testGetAccount_notNull() throws Exception {
        // Create first account, should return same account.
        KinAccount kinAccount = createAccount();
        KinAccount sameAccount = kinClient.getAccount();

        assertNotNull(sameAccount);
        assertEquals(kinAccount, sameAccount);
    }

    @Test
    public void testHasAccounts_noAccount() throws Exception {
        // No account created
        // Check if has account
        assertFalse(kinClient.hasAccount());
    }

    @Test
    public void testHasAccounts() throws Exception {
        // Create first account
        createAccount();
        // Check if has account
        assertTrue(kinClient.hasAccount());
    }

    private KinAccount createAccount() throws Exception {
        return kinClient.createAccount(PASSPHRASE);
    }
}