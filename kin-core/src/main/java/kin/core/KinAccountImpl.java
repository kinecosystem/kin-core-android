package kin.core;

import android.support.annotation.NonNull;
import java.math.BigDecimal;
import kin.core.exception.AccountDeletedException;
import kin.core.exception.OperationFailedException;
import kin.core.exception.PassphraseException;


final class KinAccountImpl extends AbstractKinAccount {

    private final Account account;
    private final TransactionSender transactionSender;
    private final AccountActivator accountActivator;
    private final BalanceQuery balanceQuery;
    private boolean isDeleted = false;

    KinAccountImpl(Account account, TransactionSender transactionSender, AccountActivator accountActivator,
        BalanceQuery balanceQuery) {
        this.account = account;
        this.transactionSender = transactionSender;
        this.accountActivator = accountActivator;
        this.balanceQuery = balanceQuery;
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
    public TransactionId sendTransactionSync(@NonNull String publicAddress, @NonNull String passphrase,
        @NonNull BigDecimal amount)
        throws OperationFailedException, PassphraseException {
        checkValidAccount();
        return transactionSender.sendTransaction(account, passphrase, publicAddress, amount);
    }

    @NonNull
    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return balanceQuery.getBalance(account);
    }

    @Override
    public void activateSync(@NonNull String passphrase) throws OperationFailedException {
        checkValidAccount();
        accountActivator.activate(account, passphrase);
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
