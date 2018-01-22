package kin.sdk.core;

/**
 * Identifier of the transaction, can be use on <a href="https://etherscan.io/">etherscan.io</a>
 * to find information about the transaction related to this TransactionId.
 */
public interface TransactionId {

    /**
     * @return the transaction id
     */
    String id();
}