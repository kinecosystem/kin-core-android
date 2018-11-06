package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

abstract class AbstractKinAccount implements KinAccount {

    @NonNull
    @Override
    public Request<Transaction> buildTransaction(@NonNull final String publicAddress, @NonNull final BigDecimal amount) {
        return new Request<>(new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount);
            }
        });
    }@NonNull
    @Override
    public Request<Transaction> buildTransaction(@NonNull final String publicAddress,
                                                 @NonNull final BigDecimal amount, @Nullable final String memo) {
        return new Request<>(new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount, memo);
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(final Transaction transaction) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(transaction);
            }
        });
    }

    @NonNull
    @Override
    public Request<Balance> getBalance() {
        return new Request<>(new Callable<Balance>() {
            @Override
            public Balance call() throws Exception {
                return getBalanceSync();
            }
        });
    }

    @NonNull
    @Override
    public Request<Void> activate() {
        return new Request<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                activateSync();
                return null;
            }
        });
    }

    @NonNull
    @Override
    public Request<Integer> getStatus() {
        return new Request<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return getStatusSync();
            }
        });
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KinAccount account = (KinAccount) obj;
        if (getPublicAddress() == null || account.getPublicAddress() == null) {
            return false;
        }
        return getPublicAddress().equals(account.getPublicAddress());
    }
}
