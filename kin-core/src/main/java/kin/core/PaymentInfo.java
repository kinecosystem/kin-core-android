package kin.core;


import java.math.BigDecimal;

/**
 * Represents payment issued on the blockchain
 */
interface PaymentInfo {

    /**
     * Transaction creation time
     */
    String createdAt();

    /**
     * Destination account public id
     */
    String destinationPublicKey();

    /**
     * Source account public id
     */
    String sourcePublicKey();

    /**
     * Payment amount in kin
     */
    BigDecimal amount();

    /**
     * Transaction id (hash)
     */
    TransactionId hash();

    /**
     * Optional bytes array, up-to 32 bytes, included on the transaction record.
     */
    byte[] memo();
}
