package kin.sdk.core;

import android.content.Context;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.core.exception.EthereumClientException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;


/**
 * A Wrapper to the stellar library.
 * Responsible for account creation/storage/retrieval, connection to Kin contract
 * retrieving balance and sending transactions
 */
final class ClientWrapper {

    private final Context context;
    private final ServiceProvider serviceProvider;
    private final KeyStore keyStore;
    private final Server server;
    private final TransactionSender transactionSender;

    ClientWrapper(Context context, ServiceProvider serviceProvider)
        throws EthereumClientException {
        this.serviceProvider = serviceProvider;
        this.context = context.getApplicationContext();
        server = initServer();
        keyStore = initKeyStore();
        transactionSender = new TransactionSender(server, keyStore, serviceProvider.getKinAsset());
    }

    private Server initServer() {
        if (serviceProvider.isMainNet()) {
            Network.usePublicNetwork();
        } else {
            Network.useTestNetwork();
        }
        return new Server(serviceProvider.getProviderUrl());
    }

    private KeyStore initKeyStore() throws EthereumClientException {
        return new KeyStore(context);
    }

    void wipeoutAccount() throws EthereumClientException {
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

    /**
     * Get balance for the specified account.
     *
     * @param account the {@link KeyPair} to check balance
     * @return the account {@link Balance}
     * @throws OperationFailedException if could not retrieve balance
     */
    Balance getBalance(Account account) throws OperationFailedException {
        Balance balance = null;
        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(account.getAccountId()));
            for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
                if (serviceProvider.getKinAsset().isKinBalance(assetBalance)) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));
                    break;
                }
            }
        } catch (IOException e) {
            throw new OperationFailedException("Could not retrieve balance");
        }
        if (balance == null) {
            throw new OperationFailedException("Kin asset not found");
        }

        return balance;
    }


    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

}
