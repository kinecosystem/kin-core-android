package kin.sdk.core;

import android.content.Context;

import java.util.List;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.CreateAccountException;
import kin.sdk.core.exception.EthereumClientException;

public class KinClient {

    private KinAccount kinAccount;
    private ClientWrapper clientWrapper;

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
        this.clientWrapper = new ClientWrapper(context, provider);
    }

    /**
     * Create the account if it hasn't yet been created.
     * Multiple calls to this method will not create an additional account.
     * Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount()} method.
     *
     * @param passphrase a passphrase provided by the user that will be used to store the account private key securely.
     * @return {@link KinAccount} the account created
     * @throws CreateAccountException if go-ethereum was unable to generate the account (unable to generate new key or
     * store the key).
     */
    public KinAccount createAccount(String passphrase) throws CreateAccountException {
        if (!hasAccount()) {
            try {
                kinAccount = new KinAccountImpl(clientWrapper, passphrase);
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
            List<EncryptedAccount> encryptedAccounts = clientWrapper.getKeyStore().loadAccounts();
            if (!encryptedAccounts.isEmpty()) {
                kinAccount = new KinAccountImpl(clientWrapper, encryptedAccounts.get(0));
            }
            return kinAccount;
        }
    }

    /**
     * @return true if there is an existing account
     */
    public boolean hasAccount() {
        return getAccount() != null;
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
            clientWrapper.getKeyStore().deleteAccount(account.encryptedAccount(), passphrase);
            kinAccount = null;
        }
    }

    /**
     * Delete all accounts. This will wipe out recursively the directory that holds all keystore files.
     * WARNING - if you don't export your account before deleting it, you will lose all your Kin.
     */
    public void wipeoutAccount() throws EthereumClientException {
        clientWrapper.wipeoutAccount();
        KinAccount account = getAccount();
        if (account != null && account instanceof KinAccountImpl) {
            ((KinAccountImpl) account).markAsDeleted();
        }
        kinAccount = null;
    }

    public ServiceProvider getServiceProvider() {
        return clientWrapper.getServiceProvider();
    }

    KinAccount importAccount(String privateEcdsaKey, String passphrase) throws OperationFailedException {
        KinAccount kinAccount = null;
     /*   try {
            KeyPair account = KeyPair.fromSecretSeed(privateEcdsaKey);
            kinAccount = new KinAccountImpl(clientWrapper, account);
        } catch (Exception e) {
            return kinAccount;
        }*/
        return kinAccount;
    }

}
