package kin.sdk.core;


import android.support.annotation.NonNull;
import java.io.IOException;
import java.math.BigDecimal;
import kin.sdk.core.ServiceProvider.KinAsset;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

class TransactionSender {

    private final Server server;
    private final KeyStore keyStore;
    private final KinAsset kinAsset;

    TransactionSender(Server server, KeyStore keyStore, KinAsset kinAsset) {
        this.server = server;
        this.keyStore = keyStore;
        this.kinAsset = kinAsset;
    }

    /**
     * Transfer amount of kinIssuer from account to the specified public address.
     *
     * @param from the sender {@link Account}
     * @param publicAddress the address to send the kinIssuer to
     * @param amount the amount of kinIssuer to send   @return {@link TransactionId} of the transaction
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws OperationFailedException another error occurred
     */
    @NonNull
    TransactionId sendTransaction(@NonNull Account from, String passphrase, @NonNull String publicAddress,
        @NonNull BigDecimal amount)
        throws OperationFailedException, PassphraseException {

        checkAddressNotEmpty(publicAddress);
        checkForNegativeAMount(amount);
        KeyPair addressee = KeyPair.fromAccountId(publicAddress);
        verifyPayToAddress(addressee);
        KeyPair secretSeedKeyPair = keyStore.decryptAccount(from, passphrase);
        AccountResponse sourceAccount = loadSourceAccount(secretSeedKeyPair);
        Transaction transaction = buildTransaction(secretSeedKeyPair, amount, addressee, sourceAccount);
        return sendTransaction(transaction);
    }

    private void checkAddressNotEmpty(@NonNull String publicAddress) throws OperationFailedException {
        if (publicAddress.isEmpty()) {
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
            .addOperation(
                new PaymentOperation.Builder(addressee, kinAsset.getStellarAsset(), amount.toString()).build())
            .build();
        transaction.sign(from);
        return transaction;
    }

    private void verifyPayToAddress(KeyPair addressee) throws OperationFailedException {
        AccountResponse addresseeAccount;
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
            if (kinAsset.isKinBalance(balance)) {
                hasTrust = true;
            }
        }
        return hasTrust;
    }
}
