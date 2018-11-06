package kin.sdk;

final class TransactionIdImpl implements TransactionId {

    private String id;

    TransactionIdImpl(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
