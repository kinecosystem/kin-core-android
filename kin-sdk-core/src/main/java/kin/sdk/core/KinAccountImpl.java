package kin.sdk.core;

import java.math.BigDecimal;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.stellar.sdk.KeyPair;


final class KinAccountImpl extends AbstractKinAccount {

    private final ClientWrapper clientWrapper;
    private final EncryptedAccount account;
    private boolean isDeleted = false;

    /**
     * Creates a new {@link KeyPair}.
     *
     * @param clientWrapper that will be use to call to Kin smart-contract.
     * @param passphrase that will be used to store the account private key securely.
     * @throws Exception if go-ethereum was unable to generate the account (unable to generate new key or store the
     * key).
     */
    KinAccountImpl(ClientWrapper clientWrapper, String passphrase) throws Exception {
        this.clientWrapper = clientWrapper;
        this.account = clientWrapper.getKeyStore().newAccount(passphrase);
    }

    /**
     * Creates a {@link KinAccount} from existing {@link KeyPair}
     *
     * @param clientWrapper that will be use to call to Kin smart-contract.
     * @param account the existing Account.
     */
    KinAccountImpl(ClientWrapper clientWrapper, EncryptedAccount account) {
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
        return clientWrapper.sendTransaction(account, publicAddress, amount);
    }

    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return clientWrapper.getBalance(account);
    }

    @Override
    public Balance getPendingBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return clientWrapper.getBalance(account);
    }

    void delete(String passphrase) throws DeleteAccountException {
        clientWrapper.getKeyStore().deleteAccount(account, passphrase);
        markAsDeleted();
    }

    void markAsDeleted() {
        isDeleted = true;
    }

    private void checkValidAccount() throws AccountDeletedException {
        if (isDeleted) {
            throw new AccountDeletedException();
        }
    }

    EncryptedAccount encryptedAccount() {
        return account;
    }
}
