package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
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
     * Create {@link Request} for getting all the transactions payments history for the current account.
     * <p> See {@link KinAccount#getTransactionsPaymentsHistorySync()} for possibles errors</p>
     *
     * @return {@code Request<List<PaymentInfo>> } PaymentInfo - the payment information for a specific payment operation in a transaction
     */
    @NonNull
    Request<List<PaymentInfo>> getTransactionsPaymentsHistory();

    /**
     * For method description see {@link KinAccount#getTransactionsPaymentsHistory()}
     * @param requestParams an optional parameters to the request, such as accountId, cursor, limit, and order.
     * Create {@link Request} for getting all the transactions payments history for a given account.
     *                        If no account has been given to the requestParams then it will be for the current account.
     * <p> See {@link KinAccount#getTransactionsPaymentsHistorySync()} for possibles errors</p>
     *
     * <p><b>Note:</b>     requestParams can have the next members:</p>
     * <p><b>accountId</b> is optional, a string, if not given then using the current account if exist. It is represents the ID of an account.</p>
     * <p><b>cursor:</b>   is optional, a paging token, specifying where to start returning records from. for example 12884905984.</p>
     * <p><b>order:</b>    is optional, an Order, currently the default is "asc".	It is represents the order in which to return rows, “asc” or “desc”.</p>
     * <p><b>limit:</b>    is optional, a number, currently the default is 10. It is represents the maximum number of records to return.</p>
     *
     * @return {@code Request<List<PaymentInfo>> } PaymentInfo - the payment information for a specific payment operation in a transaction
     */
    @NonNull
    Request<List<PaymentInfo>> getTransactionsPaymentsHistory(TransactionHistoryRequestParams requestParams);

    /**
     * Get the list of the transactions payments history for the current account.
     * If using this method then the default is currently the last 10.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return a list of payments for a given account
     * @throws OperationFailedException operation fail error
     */
    @NonNull
    List<PaymentInfo> getTransactionsPaymentsHistorySync() throws  OperationFailedException;

    /**
     * * For method description see {@link KinAccount#getTransactionsPaymentsHistorySync()}
     * @param requestParams an optional parameters to the request, such as accountId, cursor, limit, and order.
     * Get the list of all the transactions payments history for a given account.
     * If no account has been given to the requestParams then it will be for the current account.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * <p><b>Note:</b>     requestParams can have the next members:</p>
     * <p><b>accountId</b> is optional, a string, if not given then using the current account if exist. It is represents the ID of an account.</p>
     * <p><b>cursor:</b>   is optional, a paging token, specifying where to start returning records from. for example 12884905984.</p>
     * <p><b>order:</b>    is optional, an Order, currently the default is "asc".	It is represents the order in which to return rows, “asc” or “desc”.</p>
     * <p><b>limit:</b>    is optional, a number, currently the default is 10. It is represents the maximum number of records to return.</p>
     *
     * @return a list of payments for a given account
     * @throws OperationFailedException operation fail error
     */
    @NonNull
    List<PaymentInfo> getTransactionsPaymentsHistorySync(TransactionHistoryRequestParams requestParams) throws  OperationFailedException; // TODO: 11/10/2018 add more exceptions to javadoc and maybe make the note more readable

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
}
