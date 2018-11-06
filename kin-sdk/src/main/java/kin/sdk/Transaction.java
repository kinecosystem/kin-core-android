package kin.sdk;

import kin.base.KeyPair;

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

    private final kin.base.Transaction stellarTransaction;

    Transaction(KeyPair destination, KeyPair source, BigDecimal amount,
                       String memo, TransactionId id, kin.base.Transaction stellarTransaction) {
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


    kin.base.Transaction getStellarTransaction() {
        return stellarTransaction;
    }
}
