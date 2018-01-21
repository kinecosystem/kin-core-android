package kin.sdk.core;

import android.content.Context;
import android.support.annotation.NonNull;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.core.exception.AccountNotFoundException;
import kin.sdk.core.exception.ClientException;
import kin.sdk.core.exception.NoKinTrustException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.stellar.sdk.Asset;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.HttpResponseException;


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
        throws ClientException {
        this.serviceProvider = serviceProvider;
        this.context = context.getApplicationContext();
        server = initServer();
        keyStore = initKeyStore();
        KeyPair issuer = KinConsts.getKinIssuer(serviceProvider);
        Asset kinAsset = Asset.createNonNativeAsset(KinConsts.KIN_ASSET_CODE, issuer);
        transactionSender = new TransactionSender(server, keyStore, issuer, kinAsset);
    }

    private Server initServer() {
        if (serviceProvider.isMainNet()) {
            Network.usePublicNetwork();
        } else {
            Network.useTestNetwork();
        }
        return new Server(serviceProvider.getProviderUrl());
    }

    /**
     * Create {@link KeyStore}, to have control over the account management.
     * And the ability to store accounts securely according to go-ethereum encryption protocol.
     * The keystore path is unique to each network id,
     * for example Ropsten network will be: ../data/kin/keystore/3/
     *
     * @throws ClientException if could not create directory to save the keystore.
     */
    private KeyStore initKeyStore() throws ClientException {
        return new KeyStore(context);
    }

    private String getKeyStorePath() {
        return new StringBuilder(context.getFilesDir().getAbsolutePath())
            .append(File.separator)
            .append("kin")
            .append(File.separator)
            .append("keystore")
            .append(File.separator)
            .append(serviceProvider.getNetworkId())
            .toString();
    }

    void wipeoutAccount() throws ClientException {
        File keystoreDir = new File(getKeyStorePath());
        if (keystoreDir.exists()) {
            deleteRecursive(keystoreDir);
        }
        // this will reset geth in-memory keystore
        initKeyStore();
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
                if (KinConsts.KIN_ASSET_CODE.equals(assetBalance.getAssetCode())) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));
                    break;
                }
            }
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(account.getAccountId());
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (balance == null) {
            throw new NoKinTrustException(account.getAccountId());
        }

        return balance;
    }


    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}
