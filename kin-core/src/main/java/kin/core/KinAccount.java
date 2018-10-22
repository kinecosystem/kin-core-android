package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.CryptoException;
import kin.core.exception.InsufficientKinException;
import kin.core.exception.OperationFailedException;
import kin.core.exception.TransactionFailedException;

/**
 * Represents an account which holds Kin.
 */
public interface KinAccount {

    /**
     * @return String the public address of the account, or null if deleted
     */
    @Nullable
    String getPublicAddress();

    /**
     * Create {@link Request} for signing and sending a transaction of the given amount in kin, to the specified public
     * address
     * <p> See {@link KinAccount#sendTransactionSync(String, BigDecimal)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    @NonNull
    Request<TransactionId> sendTransaction(@NonNull String publicAddress, @NonNull BigDecimal amount);

    /**
     * Create {@link Request} for signing and sending a transaction of the given amount in kin, to the specified public
     * address
     * <p> See {@link KinAccount#sendTransactionSync(String, BigDecimal)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @param memo An optional string, can contain a utf-8 string up to 28 bytes in length, included on the transaction
     * record.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    @NonNull
    Request<TransactionId> sendTransaction(@NonNull String publicAddress, @NonNull BigDecimal amount,
        @Nullable String memo);

    /**
     * Create, sign and send a transaction of the given amount in kin to the specified public address
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return TransactionId the transaction identifier
     * @throws AccountNotFoundException if the sender or destination account was not created
     * @throws AccountNotActivatedException if the sender or destination account is not activated
     * @throws InsufficientKinException if account balance has not enough kin
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details
     * @throws OperationFailedException other error occurred
     */
    @NonNull
    TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount)
        throws OperationFailedException;

    /**
     * Create, sign and send a transaction of the given amount in kin to the specified public address
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @param memo An optional string, can contain a utf-8 string up to 28 bytes in length, included on the transaction
     * record.
     * @return TransactionId the transaction identifier
     * @throws AccountNotFoundException if the sender or destination account was not created
     * @throws AccountNotActivatedException if the sender or destination account is not activated
     * @throws InsufficientKinException if account balance has not enough kin
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details
     * @throws OperationFailedException other error occurred
     */
    @NonNull
    TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount, @Nullable String memo)
        throws OperationFailedException;

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
     * <p> See {@link KinAccount#activateSync()} for possibles errors</p>
     *
     * @return {@code Request<Void>}
     */
    @NonNull
    Request<Void> activate();

    /**
     * Allow an account to receive kin.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @throws AccountNotFoundException if account is not created
     * @throws TransactionFailedException if activation transaction failed, contains blockchain failure details
     * @throws OperationFailedException any other error
     */
    void activateSync() throws OperationFailedException;

    /**
     * Get current account status on blockchain network.
     *
     * @return account status, either {@link AccountStatus#NOT_CREATED}, {@link AccountStatus#NOT_ACTIVATED} or {@link
     * AccountStatus#ACTIVATED}
     * @throws OperationFailedException any other error
     */
    @AccountStatus
    int getStatusSync() throws OperationFailedException;

    /**
     * Create {@link Request} for getting current account status on blockchain network.
     * <p> See {@link KinAccount#getStatusSync()} for possibles errors</p>
     *
     * @return account status, either {@link AccountStatus#NOT_CREATED}, {@link AccountStatus#NOT_ACTIVATED} or {@link
     * AccountStatus#ACTIVATED}
     */
    Request<Integer> getStatus();

    /**
     * Creates and adds listener for balance changes of this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addBalanceListener(@NonNull final EventListener<Balance> listener);

    /**
     * Creates and adds listener for payments concerning this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addPaymentListener(@NonNull final EventListener<PaymentInfo> listener);

    /**
     * Creates and adds listener for account creation event, use returned {@link ListenerRegistration} to stop
     * listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addAccountCreationListener(final EventListener<Void> listener);

    /**
     * Export the account data as a JSON string. The seed is encrypted.
     *
     * @param passphrase The passphrase with which to encrypt the seed
     * @return A JSON representation of the data as a string
     */
    String export(@NonNull String passphrase) throws CryptoException;
}
