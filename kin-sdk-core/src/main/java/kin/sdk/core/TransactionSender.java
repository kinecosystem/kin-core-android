package kin.sdk.core;


import static kin.sdk.core.Utils.checkNotNull;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.core.ServiceProvider.KinAsset;
import kin.sdk.core.exception.AccountNotActivatedException;
import kin.sdk.core.exception.AccountNotFoundException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import kin.sdk.core.exception.TransactionFailedException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.HttpResponseException;
import org.stellar.sdk.responses.SubmitTransactionResponse;

class TransactionSender {

    private final Server server; //horizon server
    private final KeyStore keyStore;
    private final KinAsset kinAsset;

    TransactionSender(Server server, KeyStore keyStore, KinAsset kinAsset) {
        this.server = server;
        this.keyStore = keyStore;
        this.kinAsset = kinAsset;
    }

    /**
     * Transfer amount of kin from account to the specified public address.
     *
     * @param from the sender {@link Account}
     * @param publicAddress the address to send the kinIssuer to
     * @param amount the amount of kinIssuer to send   @return {@link TransactionId} of the transaction
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws AccountNotFoundException if the sender or destination account not created yet
     * @throws AccountNotActivatedException if the sender or destination account is not activated
     * @throws TransactionFailedException if stellar transaction failed, contains stellar horizon error codes
     * @throws OperationFailedException other error occurred
     */
    @NonNull
    TransactionId sendTransaction(@NonNull Account from, @NonNull String passphrase, @NonNull String publicAddress,
        @NonNull BigDecimal amount)
        throws OperationFailedException, PassphraseException {

        checkParams(from, passphrase, publicAddress, amount);
        KeyPair addressee = generateAddresseeKeyPair(publicAddress);
        verifyAddresseeAccount(addressee);
        KeyPair secretSeedKeyPair = keyStore.decryptAccount(from, passphrase);
        AccountResponse sourceAccount = loadSourceAccount(secretSeedKeyPair);
        Transaction transaction = buildTransaction(secretSeedKeyPair, amount, addressee, sourceAccount);
        return sendTransaction(transaction);
    }

    private void checkParams(@NonNull Account from, @NonNull String passphrase, @NonNull String publicAddress,
        @NonNull BigDecimal amount) {
        checkNotNull(from, "account");
        checkNotNull(passphrase, "passphrase");
        checkNotNull(amount, "amount");
        checkAddressNotEmpty(publicAddress);
        checkForNegativeAmount(amount);
    }

    @SuppressWarnings("ConstantConditions")
    private void checkAddressNotEmpty(@NonNull String publicAddress) {
        if (publicAddress == null || publicAddress.isEmpty()) {
            throw new IllegalArgumentException("Addressee not valid - public address can't be null or empty");
        }
    }

    private void checkForNegativeAmount(@NonNull BigDecimal amount) {
        if (amount.signum() == -1) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    @NonNull
    private KeyPair generateAddresseeKeyPair(@NonNull String publicAddress) throws OperationFailedException {
        try {
            return KeyPair.fromAccountId(publicAddress);
        } catch (Exception e) {
            throw new OperationFailedException("Invalid addressee public address format", e);
        }
    }

    @NonNull
    private Transaction buildTransaction(@NonNull KeyPair from, @NonNull BigDecimal amount, KeyPair addressee,
        AccountResponse sourceAccount) {

        Transaction transaction = new Transaction.Builder(sourceAccount)
            .addOperation(
                new PaymentOperation.Builder(addressee, kinAsset.getStellarAsset(), amount.toString()).build())
            .build();
        transaction.sign(from);
        return transaction;
    }

    private void verifyAddresseeAccount(KeyPair addressee) throws OperationFailedException {
        AccountResponse addresseeAccount;
        addresseeAccount = loadAccount(addressee);
        checkKinTrust(addresseeAccount);
    }

    private AccountResponse loadAccount(@NonNull KeyPair from) throws OperationFailedException {
        AccountResponse sourceAccount;
        try {
            sourceAccount = server.accounts().account(from);
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(from.getAccountId());
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (sourceAccount == null) {
            throw new OperationFailedException("can't retrieve data for account " + from.getAccountId());
        }
        return sourceAccount;
    }

    private void checkKinTrust(AccountResponse accountResponse) throws AccountNotActivatedException {
        if (!kinAsset.hasKinTrust(accountResponse)) {
            throw new AccountNotActivatedException(accountResponse.getKeypair().getAccountId());
        }
    }

    private AccountResponse loadSourceAccount(@NonNull KeyPair from) throws OperationFailedException {
        AccountResponse sourceAccount;
        sourceAccount = loadAccount(from);
        checkKinTrust(sourceAccount);
        return sourceAccount;
    }

    @NonNull
    private TransactionId sendTransaction(Transaction transaction) throws OperationFailedException {
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            if (response == null) {
                throw new OperationFailedException("can't get transaction response");
            }
            if (response.isSuccess()) {
                return new TransactionIdImpl(response.getHash());
            } else {
                throw Utils.createTransactionException(response);
            }
        } catch (
            IOException e)

        {
            throw new OperationFailedException(e);
        }
    }
}
