package kin.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import java.math.BigDecimal;
import kin.core.exception.OperationFailedException;
import kin.core.exception.PassphraseException;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;


/**
 * A Wrapper to the stellar library.
 * Responsible for account creation/storage/retrieval, connection to Kin contract
 * retrieving balance and sending transactions
 */
class ClientWrapper {

    private static final String STORE_NAME = "KinKeyStore";
    private final ServiceProvider serviceProvider;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final BalanceQuery balanceQuery;

    ClientWrapper(Context context, ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        Server server = initServer();
        keyStore = initKeyStore(context.getApplicationContext());
        transactionSender = new TransactionSender(server, keyStore, serviceProvider.getKinAsset());
        accountActivator = new AccountActivator(server, keyStore, serviceProvider.getKinAsset());
        balanceQuery = new BalanceQuery(server, serviceProvider.getKinAsset());
    }

    @VisibleForTesting
    ClientWrapper(ServiceProvider serviceProvider, KeyStore keyStore, TransactionSender transactionSender,
        AccountActivator accountActivator, BalanceQuery balanceQuery) {
        this.serviceProvider = serviceProvider;
        this.keyStore = keyStore;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.balanceQuery = balanceQuery;
    }

    private Server initServer() {
        if (serviceProvider.isMainNet()) {
            Network.usePublicNetwork();
        } else {
            Network.useTestNetwork();
        }
        return new Server(serviceProvider.getProviderUrl());
    }

    private KeyStore initKeyStore(Context context) {
        SharedPrefStore store = new SharedPrefStore(context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE));
        Encryptor encryptor = EncryptorFactory.create(context, store);
        return new KeyStoreImpl(store, encryptor);
    }

    void wipeoutAccounts() {
        keyStore.clearAllAccounts();
    }

    @NonNull
    KeyStore getKeyStore() {
        return keyStore;
    }

    @NonNull
    TransactionId sendTransaction(@NonNull Account from, String passphrase, @NonNull String publicAddress,
        @NonNull BigDecimal amount) throws OperationFailedException, PassphraseException {
        return transactionSender.sendTransaction(from, passphrase, publicAddress, amount);
    }

    Balance getBalance(Account account) throws OperationFailedException {
        return balanceQuery.getBalance(account);
    }

    void activateAccount(Account account, String passphrase) throws OperationFailedException {
        accountActivator.activate(account, passphrase);
    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}
