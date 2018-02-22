package kin.core;


import java.math.BigDecimal;

class PaymentInfoImpl implements PaymentInfo {

    private final String createdAt;
    private final String destinationPublicKey;
    private final String sourcePublicKey;
    private final BigDecimal amount;
    private final TransactionId hash;
    private final String memo;

    PaymentInfoImpl(String createdAt, String destinationPublicKey, String sourcePublicKey, BigDecimal amount,
        TransactionId hash, String memo) {
        this.createdAt = createdAt;
        this.destinationPublicKey = destinationPublicKey;
        this.sourcePublicKey = sourcePublicKey;
        this.amount = amount;
        this.hash = hash;
        this.memo = memo;
    }

    @Override
    public String createdAt() {
        return createdAt;
    }

    @Override
    public String destinationPublicKey() {
        return destinationPublicKey;
    }

    @Override
    public String sourcePublicKey() {
        return sourcePublicKey;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public TransactionId hash() {
        return hash;
    }

    @Override
    public String memo() {
        return memo;
    }
}
