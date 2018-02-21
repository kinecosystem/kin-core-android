package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.OperationFailedException;
import kin.core.exception.PassphraseException;
import kin.core.exception.TransactionFailedException;

public interface KinAccount {

    /**
     * @return String the public address of the account, or null if deleted
     */
    @Nullable
    String getPublicAddress();

    /**
     * Create {@link Request} for signing and sending a transaction of the given amount in kin, to the specified public
     * address
     * <p> See {@link KinAccount#sendTransactionSync(String, String, BigDecimal)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @param passphrase the passphrase used to create the account
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    @NonNull
    Request<TransactionId> sendTransaction(@NonNull String publicAddress, @NonNull String passphrase,
        @NonNull BigDecimal amount);

    /**
     * Create {@link Request} for signing and sending a transaction of the given amount in kin, to the specified public
     * address
     * <p> See {@link KinAccount#sendTransactionSync(String, String, BigDecimal)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @param passphrase the passphrase used to create the account
     * @param memo An optional string, up-to 28 characters, included on the transaction record.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    @NonNull
    Request<TransactionId> sendTransaction(@NonNull String publicAddress, @NonNull String passphrase,
        @NonNull BigDecimal amount, @Nullable String memo);

    /**
     * Create, sign and send a transaction of the given amount in kin to the specified public address
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return TransactionId the transaction identifier
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws AccountNotFoundException if the sender or destination account was not created
     * @throws AccountNotActivatedException if the sender or destination account is not activated
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details
     * @throws OperationFailedException other error occurred
     */
    @NonNull
    TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull String passphrase,
        @NonNull BigDecimal amount)
        throws OperationFailedException, PassphraseException;

    /**
     * Create, sign and send a transaction of the given amount in kin to the specified public address
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @param memo An optional string, up-to 28 characters, included on the transaction record.
     * @return TransactionId the transaction identifier
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws AccountNotFoundException if the sender or destination account was not created
     * @throws AccountNotActivatedException if the sender or destination account is not activated
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details
     * @throws OperationFailedException other error occurred
     */
    @NonNull
    TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull String passphrase,
        @NonNull BigDecimal amount, @Nullable String memo)
        throws OperationFailedException, PassphraseException;

    /**
     * Create {@link Request} for getting the current confirmed balance in kin
     * <p> See {@link KinAccount#getBalanceSync()} for possibles errors</p>
     *
     * @return {@code Request<Balance>} Balance - the balance in kin
     */
    @NonNull
    Request<Balance> getBalance();

    /**
     * Get the current confirmed balance in kin
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the balance in kin
     * @throws AccountNotFoundException if account was not created
     * @throws AccountNotActivatedException if account is not activated
     * @throws OperationFailedException any other error
     */
    @NonNull
    Balance getBalanceSync() throws OperationFailedException;

    /**
     * Create {@link Request} for allowing an account to receive kin.
     * <p> See {@link KinAccount#activateSync(String)} for possibles errors</p>
     *
     * @param passphrase the passphrase used to create the account
     * @return {@code Request<Void>}
     */
    @NonNull
    Request<Void> activate(@NonNull String passphrase);

    /**
     * Allow an account to receive kin.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param passphrase the passphrase used to create the account
     * @throws AccountNotFoundException if account is not created
     * @throws TransactionFailedException if activation transaction failed, contains blockchain failure details
     * @throws OperationFailedException any other error
     */
    void activateSync(@NonNull String passphrase) throws OperationFailedException;

    /**
     * Creates a watcher for every payment done in kin that involves this account.
     *
     * @return {@link PaymentWatcher} watching payments
     */
    PaymentWatcher createPaymentWatcher();
}
