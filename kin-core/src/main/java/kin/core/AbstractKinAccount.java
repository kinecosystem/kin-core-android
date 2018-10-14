package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

abstract class AbstractKinAccount implements KinAccount {

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(@NonNull final String publicAddress,
        @NonNull final BigDecimal amount) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(publicAddress, amount, null);
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(@NonNull final String publicAddress, @NonNull final BigDecimal amount,
        @Nullable final String memo) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(publicAddress, amount, memo);
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
    public Request<List<PaymentInfo>> getTransactionsPaymentsHistory() {
        return new Request<>(new Callable<List<PaymentInfo>>() {
            @Override
            public List<PaymentInfo> call() throws Exception {
                return getTransactionsPaymentsHistorySync();
            }
        });
    }

    @NonNull
    @Override
    public Request<List<PaymentInfo>> getTransactionsPaymentsHistory(final TransactionHistoryRequestParams requestParams) {
        return new Request<>(new Callable<List<PaymentInfo>>() {
            @Override
            public List<PaymentInfo> call() throws Exception {
                return getTransactionsPaymentsHistorySync(requestParams);
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
