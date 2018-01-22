package kin.sdk.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.support.test.runner.AndroidJUnit4;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kin.sdk.core.Config.EcdsaAccount;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KinAccountTest extends BaseTest {

    private final String PASSPHRASE = "testPassphrase";
    private final String TO_ADDRESS = "0x82CdC15705CE9f4565DDa07d78c92ff3d2717854";

    /**
     * First new account with 0 TOKEN and 0 ETH.
     */
    private KinAccount kinAccount;

    /**
     * Imported account via private ECDSA Key, from testConfig.json
     * The first account (importedAccounts.get(0)) will have 1000 TOKEN and 100 ETH, and can sendTransactions.
     * All other accounts (1-9) will have only 100 ETH.
     */
    private List<KinAccount> importedAccounts = new ArrayList<>(10);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        kinAccount = kinClient.createAccount(PASSPHRASE);
        importAccounts();
    }

    /**
     * Import all accounts from {@link Config}.
     */
    private void importAccounts() throws OperationFailedException {
        List<EcdsaAccount> accounts = config.getAccounts();
        for (EcdsaAccount account : accounts) {
            KinAccount importedAccount = kinClient.importAccount(account.getKey(), PASSPHRASE);
            importedAccounts.add(importedAccount);
        }
    }

    @Test
    public void testGetPublicAddress() {
        String address = kinAccount.getPublicAddress();

        assertNotNull(address);
        assertThat(address, CoreMatchers.startsWith("0x"));
        assertEquals(40, address.substring(2, address.length()).length());
    }

    @Test
    public void exportKeyStore() throws PassphraseException, OperationFailedException {
        String exportedKeyStore = kinAccount.exportKeyStore(PASSPHRASE, "newPassphrase");

        assertNotNull(exportedKeyStore);
        assertThat(exportedKeyStore, CoreMatchers.containsString("address"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("crypto"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("cipher"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("ciphertext"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("cipherparams"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("iv"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("kdf"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("kdfparams"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("dklen"));
        assertThat(exportedKeyStore, CoreMatchers.containsString("salt"));
    }

    @Test(expected = PassphraseException.class)
    public void exportKeyStore_wrongPassphrase() throws Exception {
        kinAccount.exportKeyStore("wrongPassphrase", "newPassphrase");
    }

    @Test(expected = AccountDeletedException.class)
    public void exportKeyStore_deletedAccount() throws Exception {
        kinClient.deleteAccount(PASSPHRASE);
        kinAccount.exportKeyStore(PASSPHRASE, "newPassphrase");
    }

    @Test(expected = AccountDeletedException.class)
    public void transaction_deletedAccount() throws Exception {
        kinClient.deleteAccount(PASSPHRASE);
        kinAccount.sendTransactionSync(TO_ADDRESS, PASSPHRASE, new BigDecimal(1));
    }

    public void getPublicKey_deletedAccount()
        throws PassphraseException, OperationFailedException, DeleteAccountException {
        kinClient.deleteAccount(PASSPHRASE);
        String publicAddress = kinAccount.getPublicAddress();
        assertEquals("", publicAddress);
    }


    @Test
    public void sendTransactionSync_negativeAmount() throws Exception {
        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage("Amount can't be negative");
        kinAccount.sendTransactionSync(TO_ADDRESS, PASSPHRASE, new BigDecimal(-1));
    }

    @Test(expected = OperationFailedException.class)
    public void sendTransactionSync_nullPublicAddress() throws Exception {
        kinAccount.sendTransactionSync(null, PASSPHRASE, new BigDecimal(0));
    }

    @Test(expected = OperationFailedException.class)
    public void sendTransactionSync_shortPublicAddress() throws Exception {
        kinAccount.sendTransactionSync("0xShortAddress", PASSPHRASE, new BigDecimal(0));
    }

    @Test(expected = OperationFailedException.class)
    public void sendTransactionSync_longPublicAddress() throws Exception {
        kinAccount
            .sendTransactionSync("0xLongAddressVeryLongMoreThan40CharsYouCanCount!!", PASSPHRASE, new BigDecimal(0));
    }

    @Test(expected = OperationFailedException.class)
    public void sendTransactionSync_illegalCharPublicAddress() throws Exception {
        kinAccount.sendTransactionSync("0xabababababababababababababababababababaX", PASSPHRASE, new BigDecimal(0));
    }

    @Test(expected = OperationFailedException.class)
    public void sendTransactionSync_emptyPublicAddress() throws Exception {
        kinAccount.sendTransactionSync("", PASSPHRASE, new BigDecimal(0));
    }

    @Test(expected = PassphraseException.class)
    public void sendTransactionSync_wrongPassphrase() throws Exception {
        kinAccount.sendTransactionSync(TO_ADDRESS, "wongPassphrase", new BigDecimal(0));
    }

    @Test
    public void sendTransactionSync_SecondTimeEmptyPassphraseFails() throws Exception {
        KinAccount senderAccount = importedAccounts.get(0);
        Balance senderBalance = senderAccount.getBalanceSync();
        BigDecimal amountToSend = new BigDecimal(10);

        senderAccount.sendTransactionSync(kinAccount.getPublicAddress(), PASSPHRASE, amountToSend);

        Balance kinAccountBalance = kinAccount.getBalanceSync();
        assertTrue(kinAccountBalance.value(0).equals("10"));

        Balance afterBalance = senderAccount.getBalanceSync();
        assertTrue((senderBalance.value().subtract(amountToSend).compareTo(afterBalance.value())) == 0);

        expectedEx.expect(PassphraseException.class);
        senderAccount.sendTransactionSync(kinAccount.getPublicAddress(), "", new BigDecimal(1));
    }

    @Test
    public void sendTransactionSync_SecondTimeNullPassphraseFails() throws Exception {
        KinAccount senderAccount = importedAccounts.get(0);
        Balance senderBalance = senderAccount.getBalanceSync();
        BigDecimal amountToSend = new BigDecimal(10);

        senderAccount.sendTransactionSync(kinAccount.getPublicAddress(), PASSPHRASE, amountToSend);

        Balance kinAccountBalance = kinAccount.getBalanceSync();
        assertTrue(kinAccountBalance.value(0).equals("10"));
        Balance afterBalance = senderAccount.getBalanceSync();
        assertTrue((senderBalance.value().subtract(amountToSend).compareTo(afterBalance.value())) == 0);

        expectedEx.expect(PassphraseException.class);
        senderAccount.sendTransactionSync(kinAccount.getPublicAddress(), null, new BigDecimal(1));
    }

    @Test
    public void sendTransactionSync() throws Exception {
        KinAccount senderAccount = importedAccounts.get(0);
        Balance senderBalance = senderAccount.getBalanceSync();
        BigDecimal amountToSend = new BigDecimal(10);

        senderAccount.sendTransactionSync(kinAccount.getPublicAddress(), PASSPHRASE, amountToSend);

        Balance kinAccountBalance = kinAccount.getBalanceSync();
        assertTrue(kinAccountBalance.value(0).equals("10"));

        Balance afterBalance = senderAccount.getBalanceSync();
        assertTrue((senderBalance.value().subtract(amountToSend).compareTo(afterBalance.value())) == 0);
    }

    @Test
    public void getBalanceSync() throws OperationFailedException {
        Balance balance = kinAccount.getBalanceSync();

        assertNotNull(balance);
        assertEquals("0", balance.value(0));
        assertEquals("0.0", balance.value(1));
    }

    @Test
    public void getPendingBalanceSync_withNoTransaction() throws Exception {
        Balance balance = kinAccount.getBalanceSync();
        Balance pendingBalance = kinAccount.getPendingBalanceSync();

        assertNotNull(pendingBalance);
        assertEquals(balance.value(), pendingBalance.value());
    }

    @Test
    public void getPendingBalanceSync() throws Exception {
        KinAccount senderAccount = importedAccounts.get(0);
        senderAccount.sendTransactionSync(kinAccount.getPublicAddress(), PASSPHRASE, new BigDecimal(10));
        Balance kinAccountPendingBalance = kinAccount.getPendingBalanceSync();

        assertNotNull(kinAccountPendingBalance);
    }
}