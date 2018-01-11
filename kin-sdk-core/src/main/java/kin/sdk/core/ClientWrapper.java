package kin.sdk.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.EthereumClientException;
import kin.sdk.core.exception.InsufficientBalanceException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import okhttp3.*;
import okhttp3.Request;
import org.ethereum.geth.Account;
import org.ethereum.geth.BoundContract;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.KeyStore;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;


/**
 * A Wrapper to the geth (go ethereum) library.
 * Responsible for account creation/storage/retrieval, connection to Kin contract
 * retrieving balance and sending transactions
 */
final class ClientWrapper {

    private static final String TAG = ClientWrapper.class.getSimpleName();

    private static final String KIN_SHARED_PREF = "kin_shared_pref";

    private Context androidContext;
    private SharedPreferences sharedPreferences;

    private ServiceProvider serviceProvider;

    private Server server;
    private KeyPair issuer;
    private Asset KIN;

    private Executor executor = Executors.newSingleThreadExecutor();

    ClientWrapper(android.content.Context androidContext, ServiceProvider serviceProvider)
        throws EthereumClientException {
        this.serviceProvider = serviceProvider;
        this.androidContext = androidContext.getApplicationContext();
        this.sharedPreferences = androidContext.getSharedPreferences(KIN_SHARED_PREF, Context.MODE_PRIVATE);
        initServer();
        initKin();
        initKeyStore();
    }

    /**
     * Create {@link EthereumClient}, that will be a connection to Ethereum network.
     *
     * @throws EthereumClientException if go-ethereum could not establish connection to the provider.
     */
    private void initServer() throws EthereumClientException {
        if (serviceProvider.isMainNet()) {
            Network.usePublicNetwork();
        } else {
            Network.useTestNetwork();
        }
        server = new Server(serviceProvider.getProviderUrl());
    }

    /**
     * Create {@link BoundContract}, that will handle all the calls to Kin smart-contract.
     *
     * @throws EthereumClientException if go-ethereum could not establish connection to Kin smart-contract.
     */
    private void initKin() throws EthereumClientException {
        issuer = createIssuer();
        KIN = Asset.createNonNativeAsset("KIN", issuer);
    }

    private KeyPair createIssuer() {
        return KeyPair.fromAccountId("GBGFNADX2FTYVCLDCVFY5ZRTVEMS4LV6HKMWOY7XJKVXMBIWVDESCJW5");
    }

    /**
     * Create {@link KeyStore}, to have control over the account management.
     * And the ability to store accounts securely according to go-ethereum encryption protocol.
     * The keystore path is unique to each network id,
     * for example Ropsten network will be: ../data/kin/keystore/3/
     *
     * @throws EthereumClientException if could not create directory to save the keystore.
     */
    private void initKeyStore() throws EthereumClientException {
        //TODO create KeyStore
    }

    public String getKeyStorePath() {
        return new StringBuilder(androidContext.getFilesDir().getAbsolutePath())
            .append(File.separator)
            .append("kin")
            .append(File.separator)
            .append("keystore")
            .append(File.separator)
            .append(serviceProvider.getNetworkId())
            .toString();
    }

    public void deleteAccount(KeyPair account, String passphrase) throws DeleteAccountException {
        sharedPreferences.edit().remove(KinConsts.ACCOUNT_SEED).apply();
        //TODO remove from KeyStore Also
    }

    public void wipeoutAccount() throws EthereumClientException {
        File keystoreDir = new File(getKeyStorePath());
        if (keystoreDir.exists()) {
            deleteRecursive(keystoreDir);
        }
        // this will reset geth in-memory keystore
        initKeyStore();
    }

    /**
     * @return {@link KeyStore} that will handle all operations related to accounts.
     */
    KeyStore getKeyStore() {
        //TODO return KeyStore
        return null;
    }

    /**
     * Transfer amount of KIN from account to the specified public address.
     *
     * @param from the sender {@link Account}
     * @param publicAddress the address to send the KIN to
     * @param amount the amount of KIN to send
     * @return {@link TransactionId} of the transaction
     * @throws InsufficientBalanceException this is never thrown - will remove completely on next version
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws OperationFailedException another error occurred
     */
    TransactionId sendTransaction(KeyPair from, String publicAddress, BigDecimal amount)
        throws InsufficientBalanceException, OperationFailedException, PassphraseException {

        // Verify public address is valid.
        if (publicAddress == null || publicAddress.isEmpty()) {
            throw new OperationFailedException("Addressee not valid - public address can't be null or empty");
        }

        // Make sure the amount is positive and the sender account has enough KIN to send.
        if (amount.signum() == -1) {
            throw new OperationFailedException("Amount can't be negative");
        }

        KeyPair addressee = KeyPair.fromAccountId(publicAddress);
        AccountResponse addresseeAccount = null;
        try {
            // First, check to make sure that the destination account exists.
            // It will throw HttpResponseException if account does not exist or there was
            // another error.
            addresseeAccount = server.accounts().account(addressee);
        } catch (IOException e) {
            throw new OperationFailedException("Addressee not found");
        }

        // Second, check the addressee has trustline with KIN.
        checkKinTrust(addresseeAccount);

        AccountResponse sourceAccount = null;
        try {
            // If there was no error, load up-to-date information of the account.
            sourceAccount = server.accounts().account(from);
        } catch (IOException e) {
            throw new OperationFailedException(e.getMessage());
        }

        // Start building the transaction.
        Transaction transaction = new Transaction.Builder(sourceAccount)
            .addOperation(new PaymentOperation.Builder(addressee, KIN, amount.toString()).build())
            .build();
        // Sign the transaction to prove you are actually the person sending it.
        transaction.sign(from);

        // And finally, send it off to Stellar!
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
            if (balance.getAssetCode().equals("KIN") &&
                balance.getAssetIssuer().getAccountId().equals(issuer.getAccountId())) {
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
    Balance getBalance(KeyPair account) throws OperationFailedException {
        Balance balance = new BalanceImpl(new BigDecimal("0"));
        try {
            AccountResponse accountResponse = server.accounts().account(account);
            for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
                if (assetBalance.getAssetCode() != null && assetBalance.getAssetCode().equals("KIN")) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));
                }
            }
        } catch (IOException e) {
            throw new OperationFailedException("Could not retrieve balance");
        }
        return balance;
    }

//    Balance getPendingBalance(Account account) throws OperationFailedException {
//        Balance balance = getBalance(account);
//        return pendingBalance.calculate(account, balance);
//    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    Account importAccount(String privateEcdsaKey, String passphrase) throws OperationFailedException {
//        if (privateEcdsaKey == null || privateEcdsaKey.isEmpty()) {
//            throw new OperationFailedException("private key not valid - can't be null or empty");
//        } else {
//            if (privateEcdsaKey.startsWith("0x")) {
//                privateEcdsaKey = privateEcdsaKey.substring(2, privateEcdsaKey.length());
//            }
//        }
//        try {
//            byte[] hexBytes = HexUtils.hexStringToByteArray(privateEcdsaKey);
//            return keyStore.importECDSAKey(hexBytes, passphrase);
//        } catch (Exception e) {
//            throw new OperationFailedException(e);
//        }
        return null;
    }

    private boolean hasEnoughBalance(KeyPair account, BigDecimal amount) throws OperationFailedException {
        Balance balance = getBalance(account);
        // (> -1) means bigger than or equals to the amount.
        return balance.value().subtract(amount).compareTo(BigDecimal.ZERO) > -1;
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    void saveSeed(String key, char[] secretSeed) {
        sharedPreferences.edit().putString(key, new String(secretSeed)).apply();
    }

    String getSeed(String key) {
        return sharedPreferences.getString(key, null);
    }

    void removeSeed(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    void createAccountAndGetXLM(KeyPair account) {
        executor.execute(() -> {
            HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("horizon-testnet.stellar.org")
                .addPathSegment("friendbot")
                .addQueryParameter("addr", account.getAccountId())
                .build();

            OkHttpClient client = new OkHttpClient();
            okhttp3.Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                if (response != null && response.body() != null) {
                    response.close();
                    makeKinTrustLine(account);
                }
            } catch (IOException e) {
                Log.d(TAG, "createAccountAndGetXLM: failed - " + e.getMessage());
            }
        });
    }

    private void makeKinTrustLine(KeyPair account) {
        AccountResponse receiving;
        try {
            receiving = server.accounts().account(account);
            Transaction allowKin = new Transaction.Builder(receiving).addOperation(
                new ChangeTrustOperation.Builder(KIN, "10000").build())
                .build();
            allowKin.sign(account);
            server.submitTransaction(allowKin);
        } catch (IOException e) {
            Log.d(TAG, "makeKinTrustLine: failed - " + e.getMessage());
        }
    }

    void getKin() {
        executor.execute(() -> {
            KeyPair account = getAccount();
            if (account == null) {
                return;
            }

            // Make sure you have localhost server, to have this working.
            HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.0.2.2")
                .port(9000)
                .addPathSegment("friendkin")
                .addQueryParameter("addr", account.getAccountId())
                .build();
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                if (response != null && response.body() != null) {
                    System.out.println(response.body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private KeyPair getAccount() {
        String seed = sharedPreferences.getString(KinConsts.ACCOUNT_SEED, null);
        if (seed == null) {
            return null;
        }
        return KeyPair.fromSecretSeed(seed);
    }
}
