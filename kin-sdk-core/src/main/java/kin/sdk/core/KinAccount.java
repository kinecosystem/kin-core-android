package kin.sdk.core;

import java.math.BigDecimal;
import kin.sdk.core.exception.AccountNotActivatedException;
import kin.sdk.core.exception.AccountNotFoundException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import kin.sdk.core.exception.TransactionFailedException;

public interface KinAccount {

    /**
     * @return String the public address of the account, or null if deleted
     */
    String getPublicAddress();

    /**
     * Create {@link Request} for signing and sending a transaction of the given amount in kin to the specified public
     * address
     * <p> See {@link KinAccount#sendTransactionSync(String, String, BigDecimal)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    Request<TransactionId> sendTransaction(String publicAddress, String passphrase, BigDecimal amount);

    /**
     * Create, sign and send a transaction of the given amount in kin to the specified public address
     * The method will accesses a horizon server on the network and should not be called on the android main thread.
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return TransactionId the transaction identifier
     * @throws PassphraseException if the transaction could not be signed with the passphrase specified
     * @throws AccountNotFoundException if the sender or destination account not created yet
     * @throws AccountNotActivatedException if the sender or destination account is not activated
     * @throws TransactionFailedException if stellar transaction failed, contains stellar horizon error codes
     * @throws OperationFailedException other error occurred
     */
    TransactionId sendTransactionSync(String publicAddress, String passphrase, BigDecimal amount)
        throws OperationFailedException, PassphraseException;

    /**
     * Create {@link Request} for getting the current confirmed balance in kin
     * <p> See {@link KinAccount#getBalanceSync()} for possibles errors</p>
     *
     * @return {@code Request<Balance>} Balance - the balance in kin
     */
    Request<Balance> getBalance();

    /**
     * Get the current confirmed balance in kin
     * The method will accesses a horizon node on the network and should not be called on the android main thread.
     *
     * @return Balance the balance in kin
     * @throws AccountNotFoundException if account not created yet
     * @throws AccountNotActivatedException if account is not activated
     * @throws OperationFailedException any other error
     */
    Balance getBalanceSync() throws OperationFailedException;

    /**
     * Create {@link Request} for allow an account to receive kin.
     * <p> See {@link KinAccount#activateSync(String)} for possibles errors</p>
     *
     * @param passphrase the passphrase used to create the account
     * @return {@code Request<Void>}
     */
    Request<Void> activate(String passphrase);

    /**
     * Allow an account to receive kin.
     * The method will accesses a horizon node on the network and should not be called on the android main thread.
     *
     * @param passphrase the passphrase used to create the account
     * @throws AccountNotFoundException if account not created yet
     * @throws TransactionFailedException if activation transaction failed, contains stellar horizon error codes
     * @throws OperationFailedException any other error
     */
    void activateSync(String passphrase) throws OperationFailedException;
}
