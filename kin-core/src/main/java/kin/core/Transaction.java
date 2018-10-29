package kin.core;

import org.stellar.sdk.KeyPair;

import java.math.BigDecimal;

public class Transaction {

    /**
     * Destination key pair.
     */
    private final KeyPair destination;
    /**
     * Source account key pair.
     */
    private final KeyPair source;

    private final BigDecimal amount;
    private final String memo;

    /**
     * The transaction hash
     */
    private final String id;

    private final org.stellar.sdk.Transaction stellarTransaction;

    public Transaction(KeyPair destination, KeyPair source, BigDecimal amount,
                       String memo, String id, org.stellar.sdk.Transaction stellarTransaction) {
        this.destination = destination;
        this.source = source;
        this.amount = amount;
        this.memo = memo;
        this.id = id; // TODO: 23/10/2018 maybe use TransactionId object here in order to keep it like today? or maybe delete TransactionId?
        this.stellarTransaction = stellarTransaction;  // TODO: 23/10/2018 create a better name
    }

    public KeyPair getDestination() {
        return destination;
    }

    public KeyPair getSource() {
        return source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public String getId() {
        return id;
    }


    org.stellar.sdk.Transaction getStellarTransaction() {
        return stellarTransaction;
    }
}
