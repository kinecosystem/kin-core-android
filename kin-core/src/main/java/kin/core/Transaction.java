package kin.core;

import org.stellar.sdk.KeyPair;

import java.math.BigDecimal;

public class Transaction {

    private final KeyPair destination;
    private final KeyPair source;

    private final BigDecimal amount;
    private final String memo;

    /**
     * The transaction hash
     */
    private final TransactionId id;

    private final org.stellar.sdk.Transaction stellarTransaction;

    Transaction(KeyPair destination, KeyPair source, BigDecimal amount,
                       String memo, TransactionId id, org.stellar.sdk.Transaction stellarTransaction) {
        this.destination = destination;
        this.source = source;
        this.amount = amount;
        this.memo = memo;
        this.id = id;
        this.stellarTransaction = stellarTransaction;
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

    public TransactionId getId() {
        return id;
    }


    org.stellar.sdk.Transaction getStellarTransaction() {
        return stellarTransaction;
    }
}
