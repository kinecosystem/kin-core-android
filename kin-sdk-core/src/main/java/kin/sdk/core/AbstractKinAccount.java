package kin.sdk.core;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

abstract class AbstractKinAccount implements KinAccount {

    @Override
    public Request<TransactionId> sendTransaction(final String publicAddress, final String passphrase,
        final BigDecimal amount) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(publicAddress, passphrase, amount);
            }
        });
    }

    @Override
    public Request<Balance> getBalance() {
        return new Request<>(new Callable<Balance>() {
            @Override
            public Balance call() throws Exception {
                return getBalanceSync();
            }
        });
    }

    @Override
    public Request<Void> activate(final String passphrase) {
        return new Request<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                activateSync(passphrase);
                return null;
            }
        });
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
