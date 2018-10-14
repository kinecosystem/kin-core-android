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
        this.accountInfoRetriever.setBlockChainEvents(this.blockchainEvents); // TODO: 12/10/2018 this is the simplest way to do it as discussed with Yossi
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
    public List<PaymentInfo> getTransactionsPaymentsHistorySync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getTransactionsPaymentsHistory(account.getAccountId());
    }

    @NonNull
    @Override
    public List<PaymentInfo> getTransactionsPaymentsHistorySync(TransactionHistoryRequestParams requestParams) throws OperationFailedException {
        String accountId = requestParams.getAccountId();
        // check only if there is no accountId in the params which is optional for the client to add. If non was found then get use the current account.
        if (TextUtils.isEmpty(accountId)) {
            requestParams.setAccountId(account.getAccountId()); // because no accountId was given then set the current account to be the "given" one.
            checkValidAccount();
        }
        return accountInfoRetriever.getTransactionsPaymentsHistory(requestParams);
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
    public BlockchainEvents blockchainEvents() {
        return blockchainEvents;
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
