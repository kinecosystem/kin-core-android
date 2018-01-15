package kin.sdk.core;

import android.content.Context;
import android.support.annotation.NonNull;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.EthereumClientException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.ethereum.geth.Account;
import org.stellar.sdk.Asset;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;


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
    private final KeyPair issuer;
    private final Asset kinAsset;

    ClientWrapper(Context context, ServiceProvider serviceProvider)
        throws EthereumClientException {
        this.serviceProvider = serviceProvider;
        this.context = context.getApplicationContext();
        server = initServer();
        issuer = KinConsts.getKinIssuer(serviceProvider);
        kinAsset = Asset.createNonNativeAsset(KinConsts.KIN_ASSET_CODE, issuer);
        keyStore = initKeyStore();
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
     * @throws EthereumClientException if could not create directory to save the keystore.
     */
    private KeyStore initKeyStore() throws EthereumClientException {
        return new KeyStore(context);
    }

    public String getKeyStorePath() {
        return new StringBuilder(context.getFilesDir().getAbsolutePath())
            .append(File.separator)
            .append("kin")
            .append(File.separator)
            .append("keystore")
            .append(File.separator)
            .append(serviceProvider.getNetworkId())
            .toString();
    }

    public void wipeoutAccount() throws EthereumClientException {
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

    /**
     * Transfer amount of kinIssuer from account to the specified public address.
     *
     * @param from the sender {@link Account}
     * @param publicAddress the address to send the kinIssuer to
     * @param amount the amount of kinIssuer to send
     * @return {@link TransactionId} of the transaction
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws OperationFailedException another error occurred
     */
    @NonNull
    TransactionId sendTransaction(@NonNull EncryptedAccount from, @NonNull String publicAddress,
        @NonNull BigDecimal amount)
        throws OperationFailedException, PassphraseException {

        checkAddressNotEmpty(publicAddress);
        checkForNegativeAMount(amount);
        KeyPair addressee = KeyPair.fromAccountId(publicAddress);
        verifyPayToAddress(addressee);
        KeyPair secretSeedKeyPair = keyStore.decryptAccount(from);
        AccountResponse sourceAccount = loadSourceAccount(secretSeedKeyPair);
        Transaction transaction = buildTransaction(secretSeedKeyPair, amount, addressee, sourceAccount);
        return sendTransaction(transaction);
    }

    private void checkAddressNotEmpty(@NonNull String publicAddress) throws OperationFailedException {
        if (publicAddress == null || publicAddress.isEmpty()) {
            throw new OperationFailedException("Addressee not valid - public address can't be null or empty");
        }
    }

    private void checkForNegativeAMount(@NonNull BigDecimal amount) throws OperationFailedException {
        if (amount.signum() == -1) {
            throw new OperationFailedException("Amount can't be negative");
        }
    }

    @NonNull
    private Transaction buildTransaction(@NonNull KeyPair from, @NonNull BigDecimal amount, KeyPair addressee,
        AccountResponse sourceAccount) {
        Transaction transaction = new Transaction.Builder(sourceAccount)
            .addOperation(new PaymentOperation.Builder(addressee, kinAsset, amount.toString()).build())
            .build();
        transaction.sign(from);
        return transaction;
    }

    private void verifyPayToAddress(KeyPair addressee) throws OperationFailedException {
        AccountResponse addresseeAccount = null;
        try {
            addresseeAccount = server.accounts().account(addressee);
        } catch (IOException e) {
            throw new OperationFailedException("Addressee not found");
        }

        // Second, check the addressee has trustline with kinIssuer.
        if (checkKinTrust(addresseeAccount)) {
            throw new OperationFailedException("Addressee don't have Kin asset trust");
        }
    }

    private AccountResponse loadSourceAccount(@NonNull KeyPair from) throws OperationFailedException {
        AccountResponse sourceAccount;
        try {
            sourceAccount = server.accounts().account(from);
        } catch (IOException e) {
            throw new OperationFailedException(e.getMessage());
        }
        return sourceAccount;
    }

    @NonNull
    private TransactionId sendTransaction(Transaction transaction) throws OperationFailedException {
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            return new TransactionIdImpl(response.getHash());
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }
    }

    private boolean checkKinTrust(AccountResponse addresseeAccount) {
        AccountResponse.Balance balances[] = addresseeAccount.getBalances();
        boolean hasTrust = false;
        for (AccountResponse.Balance balance : balances) {
            if (KinConsts.KIN_ASSET_CODE.equals(balance.getAssetCode()) &&
                balance.getAssetIssuer() != null &&
                issuer.getAccountId().equals(balance.getAssetIssuer().getAccountId())) {
                hasTrust = true;
            }
        }
        return hasTrust;
    }

    /**
     * Get balance for the specified account.
     *
     * @param account the {@link KeyPair} to check balance
     * @return the account {@link Balance}
     * @throws OperationFailedException if could not retrieve balance
     */
    Balance getBalance(EncryptedAccount account) throws OperationFailedException {
        Balance balance = null;
        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(account.getAccountId()));
            for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
                if (KinConsts.KIN_ASSET_CODE.equals(assetBalance.getAssetCode())) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));
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

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}
