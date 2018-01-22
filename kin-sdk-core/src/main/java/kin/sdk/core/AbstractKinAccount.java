package kin.sdk.core;

import java.math.BigDecimal;

abstract class AbstractKinAccount implements KinAccount {

    @Override
    public Request<TransactionId> sendTransaction(final String publicAddress, final String passphrase,
        final BigDecimal amount) {
        return new Request<>(() -> sendTransactionSync(publicAddress, passphrase, amount));
    }

    @Override
    public Request<Balance> getBalance() {
        return new Request<>(this::getBalanceSync);
    }

    @Override
    public Request<Balance> getPendingBalance() {
        return new Request<>(this::getPendingBalanceSync);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KinAccount account = (KinAccount) obj;
        return getPublicAddress().equals(account.getPublicAddress());
    }
}
