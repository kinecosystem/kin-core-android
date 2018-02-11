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
    private final List<KinAccountImpl> kinAccounts = new ArrayList<>(1);

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
        loadAccounts();
    }

    @VisibleForTesting
    KinClient(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        keyStore = clientWrapper.getKeyStore();
        loadAccounts();
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
     * Creates and adds an account.
     * <p>Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount(int)} method.</p>
     *
     * @param passphrase a passphrase provided by the user that will be used to store the account private key securely.
     * @return {@link KinAccount} the account created store the key).
     */
    public KinAccount addAccount(String passphrase) throws CreateAccountException {
        Account account = keyStore.newAccount();
        KinAccountImpl newAccount = new KinAccountImpl(clientWrapper, account);
        kinAccounts.add(newAccount);
        return newAccount;
    }

    /**
     * Return an account input index, returns an account that has previously been create and stored on the device
     * via the {@link #addAccount(String)} method.
     *
     * @return the account at the input index or null if there is no such account
     */
    public KinAccount getAccount(int index) {
        if (index >= 0 && kinAccounts.size() > index) {
            return kinAccounts.get(index);
        }
        return null;
    }

    /**
     * @return true if there is an existing account
     */
    public boolean hasAccount() {
        return getAccountCount() != 0;
    }

    /**
     * Returns the number of existing accounts
     */
    public int getAccountCount() {
        return kinAccounts.size();
    }

    /**
     * Deletes the account at input index (if it exists)
     *
     * @param passphrase the passphrase used when the account was created
     */
    public void deleteAccount(int index, String passphrase) throws DeleteAccountException {
        if (index >= 0 && getAccountCount() > index) {
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
