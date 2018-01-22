package kin.sdk.core;

import android.content.Context;

import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;

import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.CreateAccountException;
import kin.sdk.core.exception.EthereumClientException;

public class KinClient {

    private KinAccount kinAccount;
    private EthClientWrapper ethClient;

    /**
     * KinClient is an account manager for a single {@link KinAccount} on
     * ethereum network.
     *
     * @param context the android application context
     * @param provider the service provider to use to connect to an ethereum node
     * @throws EthereumClientException if could not connect to service provider or connection problem with Kin
     * smart-contract problems.
     */
    public KinClient(Context context, ServiceProvider provider) throws EthereumClientException {
        this.ethClient = new EthClientWrapper(context, provider);
    }

    /**
     * Create the account if it hasn't yet been created.
     * Multiple calls to this method will not create an additional account.
     * Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount()} method.
     *
     * @param passphrase a passphrase provided by the user that will be used to store the account private key securely.
     * @return KinAccount the account created
     * @throws CreateAccountException if go-ethereum was unable to generate the account (unable to generate new key or
     * store the key).
     */
    public KinAccount createAccount(String passphrase) throws CreateAccountException {
        if (!hasAccount()) {
            try {
                kinAccount = new KinAccountImpl(ethClient, passphrase);
            } catch (Exception e) {
                throw new CreateAccountException(e);
            }
        }
        return getAccount();
    }

    /**
     * The method will return an account that has previously been create and stored on the device
     * via the {@link #createAccount(String)} method.
     *
     * @return the account if it has been created or null if there is no such account
     */
    public KinAccount getAccount() {
        if (kinAccount != null) {
            return kinAccount;
        } else {
            Accounts accounts = ethClient.getKeyStore().getAccounts();
            Account account;
            try {
                account = accounts.get(0);
            } catch (Exception e) {
                //There is no account
                return null;
            }
            // The Account is not null
            kinAccount = new KinAccountImpl(ethClient, account);
        }
        return kinAccount;
    }

    /**
     * @return true if there is an existing account
     */
    public boolean hasAccount() {
        if (kinAccount != null) {
            return true;
        } else {
            Accounts accounts = ethClient.getKeyStore().getAccounts();
            return accounts != null && accounts.size() > 0;
        }
    }

    /**
     * Deletes the account (if it exists)
     * WARNING - if you don't export the account before deleting it, you will lose all your Kin.
     *
     * @param passphrase the passphrase used when the account was created
     */
    public void deleteAccount(String passphrase) throws DeleteAccountException {
        KinAccountImpl account = (KinAccountImpl) getAccount();
        if (account != null) {
            account.delete(passphrase);
            kinAccount = null;
        }
    }

    /**
     * Delete all accounts. This will wipe out recursively the directory that holds all keystore files.
     * WARNING - if you don't export your account before deleting it, you will lose all your Kin.
     */
    public void wipeoutAccount() throws EthereumClientException {
        ethClient.wipeoutAccount();
        KinAccount account = getAccount();
        if (account != null && account instanceof KinAccountImpl) {
            ((KinAccountImpl) account).markAsDeleted();
        }
        kinAccount = null;
    }

    public ServiceProvider getServiceProvider() {
        return ethClient.getServiceProvider();
    }

    KinAccount importAccount(String privateEcdsaKey, String passphrase) throws OperationFailedException {
        Account account = ethClient.importAccount(privateEcdsaKey, passphrase);
        KinAccount kinAccount = null;
        if (account != null) {
            kinAccount = new KinAccountImpl(ethClient, account);
        }
        return kinAccount;
    }
}
