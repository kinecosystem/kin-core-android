package kin.sdk.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import kin.sdk.core.exception.CreateAccountException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.LoadAccountException;

public class KinClient {

    private final KeyStore keyStore;
    private final ClientWrapper clientWrapper;
    @NonNull
    private final List<KinAccountImpl> kinAccounts = new ArrayList<>();

    /**
     * KinClient is an account manager for a single {@link KinAccount} on
     * ethereum network.
     *
     * @param context the android application context
     * @param provider the service provider to use to connect to an ethereum node
     */
    public KinClient(Context context, ServiceProvider provider) {
        this.clientWrapper = new ClientWrapper(context, provider);
        keyStore = clientWrapper.getKeyStore();
    }

    @VisibleForTesting
    KinClient(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        keyStore = clientWrapper.getKeyStore();
    }

    /**
     * Create the account or returns the already created at index 0.
     * <p>Multiple calls to this method will not create an additional account, use {@link #addAccount(String)} for
     * creating multiple accounts.</p>
     * Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount()} method.
     *
     * @param passphrase a passphrase provided by the user that will be used to store the account private key securely.
     * @return {@link KinAccount} the account created store the key).
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public KinAccount createAccount(String passphrase) throws CreateAccountException {
        if (!hasAccount()) {
            Account account = keyStore.newAccount();
            kinAccounts.add(new KinAccountImpl(clientWrapper, account));
        }
        return getAccount();
    }

    /**
     * Creates and adds an account.
     * <p>Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount()} method.</p>
     *
     * @param passphrase a passphrase provided by the user that will be used to store the account private key securely.
     * @return {@link KinAccount} the account created store the key).
     */
    public KinAccount addAccount(String passphrase) throws CreateAccountException {
        if (kinAccounts.isEmpty()) {
            loadAccounts();
        }
        Account account = keyStore.newAccount();
        KinAccountImpl newAccount = new KinAccountImpl(clientWrapper, account);
        kinAccounts.add(newAccount);
        return newAccount;
    }

    /**
     * Returns an account that has been previously created and stored on the device
     * via the {@link #createAccount(String)} method.
     *
     * @return the account if it has been created or null if there is no such account
     */
    @Deprecated
    public KinAccount getAccount() {
        return getAccount(0);
    }

    /**
     * Return an account input index, returns an account that has previously been create and stored on the device
     * via the {@link #createAccount(String)} method.
     *
     * @return the account at the input index or null if there is no such account
     */
    public KinAccount getAccount(int index) {
        if (kinAccounts.isEmpty()) {
            loadAccounts();
        }
        if (kinAccounts.size() > index) {
            return kinAccounts.get(index);
        }
        return null;
    }

    private void loadAccounts() {
        List<Account> accounts = null;
        try {
            accounts = clientWrapper.getKeyStore().loadAccounts();
        } catch (LoadAccountException e) {
            e.printStackTrace();
        }
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                kinAccounts.add(new KinAccountImpl(clientWrapper, account));
            }
        }
    }

    /**
     * @return true if there is an existing account
     */
    public boolean hasAccount() {
        return getAccount(0) != null;
    }

    /**
     * Returns the number of existing accounts
     */
    public int getAccountsCount() {
        if (kinAccounts.isEmpty()) {
            loadAccounts();
        }
        return kinAccounts.size();
    }

    /**
     * Deletes the account at index 0 (if it exists)
     *
     * @param passphrase the passphrase used when the account was created
     */
    @Deprecated
    public void deleteAccount(String passphrase) throws DeleteAccountException {
        deleteAccount(0, passphrase);
    }

    /**
     * Deletes the account at input index (if it exists)
     *
     * @param passphrase the passphrase used when the account was created
     */
    public void deleteAccount(int index, String passphrase) throws DeleteAccountException {
        if (getAccountsCount() > index) {
            keyStore.deleteAccount(index);
            KinAccountImpl removedAccount = kinAccounts.remove(index);
            removedAccount.markAsDeleted();
        }
    }

    /**
     * Delete all kinAccounts. This will wipe out recursively the directory that holds all keystore files.
     * WARNING - if you don't export your account before deleting it, you will lose all your Kin.
     */
    public void wipeoutAccount() {
        if (kinAccounts.isEmpty()) {
            loadAccounts();
        }
        clientWrapper.wipeoutAccounts();
        for (KinAccountImpl kinAccount : kinAccounts) {
            kinAccount.markAsDeleted();
        }
        kinAccounts.clear();
    }

    public ServiceProvider getServiceProvider() {
        return clientWrapper.getServiceProvider();
    }

}
