package kin.core;

import static kin.core.Utils.checkNotEmpty;
import static kin.core.Utils.checkNotNull;

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
    private final Environment environment;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEventsCreator blockchainEventsCreator;
    @NonNull
    private final List<KinAccountImpl> kinAccounts = new ArrayList<>(1);

    public KinClient(@NonNull Context context, @NonNull Environment environment, String appId) {
        this(context, environment, appId,"");
    }

    public KinClient(@NonNull Context context, @NonNull Environment environment, @NonNull String appId, @NonNull String storeKey) {
        checkNotNull(storeKey, "storeKey");
        checkNotNull(context, "context");
        checkNotNull(environment, "environment");
        validateAppId(appId);
        this.environment = environment;
        Server server = initServer();
        keyStore = initKeyStore(context.getApplicationContext(), storeKey);
        transactionSender = new TransactionSender(server, environment.getKinAsset(), appId);
        accountActivator = new AccountActivator(server, environment.getKinAsset());
        accountInfoRetriever = new AccountInfoRetriever(server, environment.getKinAsset());
        blockchainEventsCreator = new BlockchainEventsCreator(server, environment.getKinAsset());
        loadAccounts();
    }

    @VisibleForTesting
    KinClient(Environment environment, KeyStore keyStore, TransactionSender transactionSender,
        AccountActivator accountActivator, AccountInfoRetriever accountInfoRetriever,
        BlockchainEventsCreator blockchainEventsCreator) {
        this.environment = environment;
        this.keyStore = keyStore;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.accountInfoRetriever = accountInfoRetriever;
        this.blockchainEventsCreator = blockchainEventsCreator;
        loadAccounts();
    }

    private Server initServer() {
        Network.use(environment.getNetwork());
        return new Server(environment.getNetworkUrl(), TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS);
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

    private void validateAppId(String appId) {
        checkNotEmpty(appId, "appId");
        if (!appId.matches("[a-zA-Z0-9]{4}")) {
            throw new IllegalArgumentException("appId must contain only upper and/or lower case letters and/or digits and that the total string length is exactly 4.\n" +
                    "for example 1234 or 2ab3 or bcda, etc.");
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

    public Environment getEnvironment() {
        return environment;
    }

    @NonNull
    private KinAccountImpl createNewKinAccount(KeyPair account) {
        return new KinAccountImpl(account, transactionSender, accountActivator, accountInfoRetriever,
            blockchainEventsCreator);
    }

}
