package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import kin.core.exception.AccountDeletedException;
import kin.core.exception.OperationFailedException;


final class KinAccountImpl extends AbstractKinAccount {

    private final Account account;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final BalanceQuery balanceQuery;
    private final PaymentWatcherCreator paymentWatcherCreator;
    private boolean isDeleted = false;

    KinAccountImpl(Account account, TransactionSender transactionSender, AccountActivator accountActivator,
        BalanceQuery balanceQuery, PaymentWatcherCreator paymentWatcherCreator) {
        this.account = account;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.balanceQuery = balanceQuery;
        this.paymentWatcherCreator = paymentWatcherCreator;
    }

    @Override
    public String getPublicAddress() {
        if (!isDeleted) {
            return account.getAccountId();
        }
        return null;
    }

    @NonNull
    @Override
    public TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount)
        throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendTransaction(account, publicAddress, amount);
    }

    @NonNull
    @Override
    public TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount,
        @Nullable String memo) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendTransaction(account, publicAddress, amount, memo);
    }

    @NonNull
    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return balanceQuery.getBalance(account);
    }

    @Override
    public void activateSync() throws OperationFailedException {
        checkValidAccount();
        accountActivator.activate(account);
    }

    @Override
    public PaymentWatcher createPaymentWatcher() {
        return paymentWatcherCreator.create(account);
    }

    void markAsDeleted() {
        isDeleted = true;
    }

    private void checkValidAccount() throws AccountDeletedException {
        if (isDeleted) {
            throw new AccountDeletedException();
        }
    }

}
