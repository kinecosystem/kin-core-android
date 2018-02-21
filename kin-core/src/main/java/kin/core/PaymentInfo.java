package kin.core;


import java.math.BigDecimal;

/**
 * Represents payment issued on the blockchain.
 */
public interface PaymentInfo {

    /**
     * Transaction creation time.
     */
    String createdAt();

    /**
     * Destination account public id.
     */
    String destinationPublicKey();

    /**
     * Source account public id.
     */
    String sourcePublicKey();

    /**
     * Payment amount in kin.
     */
    BigDecimal amount();

    /**
     * Transaction id (hash).
     */
    TransactionId hash();

    /**
     * An optional string, up-to 28 characters, included on the transaction record.
     */
    String memo();
}
