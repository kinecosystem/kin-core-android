package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

abstract class AbstractKinAccount implements KinAccount {

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(@NonNull final String publicAddress, @NonNull final String passphrase,
        @NonNull final BigDecimal amount) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(publicAddress, passphrase, amount, null);
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(@NonNull final String publicAddress, @NonNull final String passphrase,
        @NonNull final BigDecimal amount, @Nullable final String memo) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(publicAddress, passphrase, amount, memo);
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
    public Request<Void> activate(@NonNull final String passphrase) {
        return new Request<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                activateSync(passphrase);
                return null;
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
