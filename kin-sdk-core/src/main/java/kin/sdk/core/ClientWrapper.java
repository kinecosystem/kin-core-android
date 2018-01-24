package kin.sdk.core;

import android.content.Context;
import android.support.annotation.NonNull;
import java.math.BigDecimal;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;


/**
 * A Wrapper to the stellar library.
 * Responsible for account creation/storage/retrieval, connection to Kin contract
 * retrieving balance and sending transactions
 */
final class ClientWrapper {

    private final Context context;
    private final ServiceProvider serviceProvider;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final BalanceQuery balanceQuery;

    ClientWrapper(Context context, ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.context = context.getApplicationContext();
        Server server = initServer();
        keyStore = initKeyStore();
        transactionSender = new TransactionSender(server, keyStore, serviceProvider.getKinAsset());
        accountActivator = new AccountActivator(server, keyStore, serviceProvider.getKinAsset());
        balanceQuery = new BalanceQuery(server, serviceProvider.getKinAsset());
    }

    private Server initServer() {
        if (serviceProvider.isMainNet()) {
            Network.usePublicNetwork();
        } else {
            Network.useTestNetwork();
        }
        return new Server(serviceProvider.getProviderUrl());
    }

    private KeyStore initKeyStore() {
        return new KeyStore(context);
    }

    void wipeoutAccount() {
        //TODO
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
