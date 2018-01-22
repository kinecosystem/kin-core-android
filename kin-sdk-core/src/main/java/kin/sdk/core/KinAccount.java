package kin.sdk.core;

import java.math.BigDecimal;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;

public interface KinAccount {

    /**
     * @return String the public address of the account
     */
    String getPublicAddress();

    /**
     * Exports the keystore json file
     *
     * @param passphrase the passphrase used to create the account
     * @param newPassphrase the exported json will be encrypted using this new passphrase. The original keystore and
     * passphrase will not change.
     * @return String the json string
     */
    String exportKeyStore(String passphrase, String newPassphrase) throws PassphraseException, OperationFailedException;

    /**
     * Create {@link Request} for signing and sending a transaction of the given amount in kin to the specified public
     * address Ethereum gas will be handled internally.
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    Request<TransactionId> sendTransaction(String publicAddress, String passphrase, BigDecimal amount);

    /**
     * Create, sign and send a transaction of the given amount in kin to the specified public address
     * Ethereum gas will be handled internally.
     * The method will accesses a blockchain
     * node on the network and should not be called on the android main thread.
     *
     * @param publicAddress the account address to send the specified kin amount
     * @param amount the amount of kin to transfer
     * @return TransactionId the transaction identifier
     */
    TransactionId sendTransactionSync(String publicAddress, String passphrase, BigDecimal amount)
        throws OperationFailedException, PassphraseException;

    /**
     * Create {@link Request} for getting the current confirmed balance in kin
     *
     * @return {@code Request<Balance>} Balance - the balance in kin
     */
    Request<Balance> getBalance();

    /**
     * Get the current confirmed balance in kin
     * The method will accesses a blockchain
     * node on the network and should not be called on the android main thread.
     *
     * @return Balance the balance in kin
     */
    Balance getBalanceSync() throws OperationFailedException;

    /**
     * Create {@link Request} for getting the pending balance in kin
     *
     * @return {@code Request<Balance>} Balance - the pending balance in kin
     */
    Request<Balance> getPendingBalance();

    /**
     * Get the pending balance in kin
     * The method will accesses a blockchain
     * node on the network and should not be called on the android main thread.
     *
     * @return Balance the balance amount in kin
     */
    Balance getPendingBalanceSync() throws OperationFailedException;
}
