package kin.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import kin.core.exception.CorruptedDataException;
import kin.core.exception.CreateAccountException;
import kin.core.exception.CryptoException;
import kin.core.exception.DeleteAccountException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;

/**
 * An account manager for a {@link KinAccount}.
 */
public class KinClient {

    private static final String STORE_NAME_PREFIX = "KinKeyStore_";
    private static final int TRANSACTIONS_TIMEOUT = 30;
    private final ServiceProvider serviceProvider;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEventsCreator blockchainEventsCreator;
    private final BackupRestore backupRestore;
    @NonNull
    private final List<KinAccountImpl> kinAccounts = new ArrayList<>(1);

    /**
     * KinClient is an account manager for a {@link KinAccount}.
     *
     * @param context the android application context
     * @param provider the service provider - provides blockchain network parameters
     * @param storeKey the key for storing this client data, different keys will store a different accounts
     */
    public KinClient(@NonNull Context context, @NonNull ServiceProvider provider, @NonNull String storeKey) {
        Utils.checkNotNull(storeKey, "storeKey");
        this.serviceProvider = provider;
        this.backupRestore = new BackupRestoreImpl();
        Server server = initServer();
        keyStore = initKeyStore(context.getApplicationContext(), storeKey);
        transactionSender = new TransactionSender(server, provider.getKinAsset());
        accountActivator = new AccountActivator(server, provider.getKinAsset());
        accountInfoRetriever = new AccountInfoRetriever(server, provider.getKinAsset());
        blockchainEventsCreator = new BlockchainEventsCreator(server, provider.getKinAsset());

        loadAccounts();
    }

    /**
     * KinClient is an account manager for a {@link KinAccount}.
     *
     * @param context the android application context
     * @param provider the service provider - provides blockchain network parameters
     */
    public KinClient(@NonNull Context context, @NonNull ServiceProvider provider) {
        this(context, provider, "");
    }

    @VisibleForTesting
    KinClient(ServiceProvider serviceProvider, KeyStore keyStore, TransactionSender transactionSender,
        AccountActivator accountActivator, AccountInfoRetriever accountInfoRetriever,
        BlockchainEventsCreator blockchainEventsCreator, BackupRestore backupRestore) {
        this.serviceProvider = serviceProvider;
        this.keyStore = keyStore;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.accountInfoRetriever = accountInfoRetriever;
        this.blockchainEventsCreator = blockchainEventsCreator;
        this.backupRestore = backupRestore;
        loadAccounts();
    }

    private Server initServer() {
        Network.use(serviceProvider.getNetwork());
        return new Server(serviceProvider.getProviderUrl(), TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS);
    }

    private KeyStore initKeyStore(Context context, String id) {
        SharedPrefStore store = new SharedPrefStore(
            context.getSharedPreferences(STORE_NAME_PREFIX + id, Context.MODE_PRIVATE));
        return new KeyStoreImpl(store, backupRestore);
    }

    private void loadAccounts() {
        List<KeyPair> accounts = null;
        try {
            accounts = keyStore.loadAccounts();
        } catch (LoadAccountException e) {
            e.printStackTrace();
        }
        if (accounts != null && !accounts.isEmpty()) {
            for (KeyPair account : accounts) {
                kinAccounts.add(createNewKinAccount(account));
            }
        }
    }

    /**
     * Creates and adds an account.
     * <p>Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount(int)} method.</p>
     *
     * @return {@link KinAccount} the account created store the key.
     */
    public @NonNull
    KinAccount addAccount() throws CreateAccountException {
        KeyPair account = keyStore.newAccount();
        return addKeyPair(account);
    }

    /**
     * Import an account from a JSON-formatted string.
     *
     * @param exportedJson The exported JSON-formatted string.
     * @param passphrase The passphrase to decrypt the secret key.
     * @return The imported account
     */
    public @NonNull
    KinAccount importAccount(@NonNull String exportedJson, @NonNull String passphrase)
        throws CryptoException, CreateAccountException, CorruptedDataException {
        KeyPair account = keyStore.importAccount(exportedJson, passphrase);
        KinAccount kinAccount = getAccountByPublicAddress(account.getAccountId());
        return kinAccount != null ? kinAccount : addKeyPair(account);
    }

    @Nullable
    private KinAccount getAccountByPublicAddress(String accountId) {
        KinAccount kinAccount = null;
        for (int i = 0; i < kinAccounts.size(); i++) {
            final KinAccount account = kinAccounts.get(i);
            if (accountId.equals(account.getPublicAddress())) {
                kinAccount = account;
            }
        }
        return kinAccount;
    }

    @NonNull
    private KinAccount addKeyPair(KeyPair account) {
        KinAccountImpl newAccount = createNewKinAccount(account);
        kinAccounts.add(newAccount);
        return newAccount;
    }

    /**
     * Returns an account at input index.
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
    @SuppressWarnings("WeakerAccess")
    public int getAccountCount() {
        return kinAccounts.size();
    }

    /**
     * Deletes the account at input index (if it exists)
     */
    public void deleteAccount(int index) throws DeleteAccountException {
        if (index >= 0 && getAccountCount() > index) {
            keyStore.deleteAccount(index);
            KinAccountImpl removedAccount = kinAccounts.remove(index);
            removedAccount.markAsDeleted();
        }
    }

    /**
     * Deletes all accounts.
     */
    @SuppressWarnings("WeakerAccess")
    public void clearAllAccounts() {
        keyStore.clearAllAccounts();
        for (KinAccountImpl kinAccount : kinAccounts) {
            kinAccount.markAsDeleted();
        }
        kinAccounts.clear();
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @NonNull
    private KinAccountImpl createNewKinAccount(KeyPair account) {
        return new KinAccountImpl(account, backupRestore, transactionSender, accountActivator, accountInfoRetriever,
            blockchainEventsCreator);
    }

}
