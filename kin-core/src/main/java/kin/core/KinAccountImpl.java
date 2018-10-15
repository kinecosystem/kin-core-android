package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.List;

import kin.core.exception.AccountDeletedException;
import kin.core.exception.OperationFailedException;
import org.stellar.sdk.KeyPair;


final class KinAccountImpl extends AbstractKinAccount {

    private final KeyPair account;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEvents blockchainEvents;
    private boolean isDeleted = false;

    KinAccountImpl(KeyPair account, TransactionSender transactionSender, AccountActivator accountActivator,
        AccountInfoRetriever accountInfoRetriever, BlockchainEventsCreator blockchainEventsCreator) {
        this.account = account;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.blockchainEvents = blockchainEventsCreator.create(account.getAccountId());
        this.accountInfoRetriever = accountInfoRetriever;
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
        return accountInfoRetriever.getBalance(account.getAccountId());
    }

    @NonNull
    @Override
    public List<PaymentInfo> getPaymentsHistorySync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getPaymentsHistory(account.getAccountId());
    }

    @NonNull
    @Override
    public List<PaymentInfo> getPaymentsHistorySync(PaymentsHistoryRequestParams requestParams) throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getPaymentsHistory(requestParams, account.getAccountId());
    }

    @Override
    public void activateSync() throws OperationFailedException {
        checkValidAccount();
        accountActivator.activate(account);
    }

    @Override
    public int getStatusSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getStatus(account.getAccountId());
    }

    @Override
    public ListenerRegistration addBalanceListener(@NonNull EventListener<Balance> listener) {
        return blockchainEvents.addBalanceListener(listener);
    }

    @Override
    public ListenerRegistration addPaymentListener(@NonNull EventListener<PaymentInfo> listener) {
        return blockchainEvents.addPaymentListener(listener);
    }

    @Override
    public ListenerRegistration addAccountCreationListener(EventListener<Void> listener) {
        return blockchainEvents.addAccountCreationListener(listener);
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
