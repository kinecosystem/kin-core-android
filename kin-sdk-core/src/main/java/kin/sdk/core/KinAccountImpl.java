package kin.sdk.core;

import java.math.BigDecimal;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;


final class KinAccountImpl extends AbstractKinAccount {

    private final ClientWrapper clientWrapper;
    private final Account account;
    private boolean isDeleted = false;

    KinAccountImpl(ClientWrapper clientWrapper, Account account) {
        this.account = account;
        this.clientWrapper = clientWrapper;
    }

    @Override
    public String getPublicAddress() {
        if (!isDeleted) {
            return account.getAccountId();
        }
        return "";
    }

    @Override
    public String exportKeyStore(String passphrase, String newPassphrase)
        throws PassphraseException, OperationFailedException {
        checkValidAccount();
        KeyStore keyStore = clientWrapper.getKeyStore();
        return keyStore.exportAccount(account, passphrase);
    }

    @Override
    public TransactionId sendTransactionSync(String publicAddress, String passphrase, BigDecimal amount)
        throws OperationFailedException, PassphraseException {
        checkValidAccount();
        return clientWrapper.sendTransaction(account, passphrase, publicAddress, amount);
    }

    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return clientWrapper.getBalance(account);
    }

    @Override
    public void activateSync(String passphrase) throws OperationFailedException {
        checkValidAccount();
        clientWrapper.activateAccount(account, passphrase);
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
