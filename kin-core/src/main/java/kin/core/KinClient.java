package kin.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import kin.core.exception.CreateAccountException;
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
    private final Environment serviceProvider;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEventsCreator blockchainEventsCreator;
    @NonNull
    private final List<KinAccountImpl> kinAccounts = new ArrayList<>(1);

    /**
     * KinClient is an account manager for a {@link KinAccount}.
     *
     * @param context the android application context
     * @param provider the service provider - provides blockchain network parameters
     * @param storeKey the key for storing this client data, different keys will store a different accounts
     */
    private KinClient(@NonNull Context context, @NonNull Environment provider, @NonNull String storeKey) {
        Utils.checkNotNull(storeKey, "storeKey");
        this.serviceProvider = provider;
        Server server = initServer();
        keyStore = initKeyStore(context.getApplicationContext(), storeKey);
        transactionSender = new TransactionSender(server, provider.getKinAsset());
        accountActivator = new AccountActivator(server, provider.getKinAsset());
        accountInfoRetriever = new AccountInfoRetriever(server, provider.getKinAsset());
        blockchainEventsCreator = new BlockchainEventsCreator(server, provider.getKinAsset());
        loadAccounts();
    }

    @VisibleForTesting
    KinClient(Environment serviceProvider, KeyStore keyStore, TransactionSender transactionSender,
        AccountActivator accountActivator, AccountInfoRetriever accountInfoRetriever,
        BlockchainEventsCreator blockchainEventsCreator) {
        this.serviceProvider = serviceProvider;
        this.keyStore = keyStore;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.accountInfoRetriever = accountInfoRetriever;
        this.blockchainEventsCreator = blockchainEventsCreator;
        loadAccounts();
    }

    private Server initServer() {
        Network.use(serviceProvider.getNetwork());
        return new Server(serviceProvider.getNetworkUrl(), TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS);
    }

    private KeyStore initKeyStore(Context context, String id) {
        SharedPrefStore store = new SharedPrefStore(
            context.getSharedPreferences(STORE_NAME_PREFIX + id, Context.MODE_PRIVATE));
        return new KeyStoreImpl(store);
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

    public Environment getServiceProvider() {
        return serviceProvider;
    }

    @NonNull
    private KinAccountImpl createNewKinAccount(KeyPair account) {
        return new KinAccountImpl(account, transactionSender, accountActivator, accountInfoRetriever,
            blockchainEventsCreator);
    }

    public static class Builder {

        private final Context context;
        private Environment environment;
        private String storeKey = "";

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Sets the blockchain network details.
         */
        public Builder setEnvironment(Environment environment) {
            this.environment = environment;
            return this;
        }

        /**
         * Sets the key for storing this KinClient data, different keys will store a different accounts.
         */
        public Builder setStoreKey(String storeKey) {
            this.storeKey = storeKey;
            return this;
        }

        /**
         * Builds a KinClient.
         */
        public KinClient build() {
            Utils.checkNotNull(environment, "environment");
            return new KinClient(context, environment, storeKey);
        }
    }
}
