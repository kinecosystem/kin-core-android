package kin.sdk.core;

import java.math.BigDecimal;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.ethereum.geth.Account;
import org.ethereum.geth.KeyStore;

final class KinAccountImpl extends AbstractKinAccount {

    private KeyStore keyStore;
    private EthClientWrapper ethClient;
    private Account account;
    private boolean isDeleted;

    /**
     * Creates a new {@link Account}.
     *
     * @param ethClientWrapper that will be use to call to Kin smart-contract.
     * @param passphrase that will be used to store the account private key securely.
     * @throws Exception if go-ethereum was unable to generate the account (unable to generate new key or store the
     * key).
     */
    KinAccountImpl(EthClientWrapper ethClientWrapper, String passphrase) throws Exception {
        this.keyStore = ethClientWrapper.getKeyStore();
        this.account = keyStore.newAccount(passphrase);
        this.ethClient = ethClientWrapper;
        isDeleted = false;
    }

    /**
     * Creates a {@link KinAccount} from existing {@link Account}
     *
     * @param ethClientWrapper that will be use to call to Kin smart-contract.
     * @param account the existing Account.
     */
    KinAccountImpl(EthClientWrapper ethClientWrapper, Account account) {
        this.keyStore = ethClientWrapper.getKeyStore();
        this.account = account;
        this.ethClient = ethClientWrapper;
        isDeleted = false;
    }

    @Override
    public String getPublicAddress() {
        if (!isDeleted) {
            return account.getAddress().getHex();
        }
        return "";
    }

    @Override
    public String exportKeyStore(String passphrase, String newPassphrase)
        throws PassphraseException, OperationFailedException {
        checkValidAccount();
        String jsonKeyStore;
        try {
            byte[] keyInBytes = keyStore.exportKey(account, passphrase, newPassphrase);
            jsonKeyStore = new String(keyInBytes, "UTF-8");
        } catch (Exception e) {
            throw new PassphraseException();
        }
        return jsonKeyStore;
    }

    @Override
    public TransactionId sendTransactionSync(String publicAddress, String passphrase, BigDecimal amount)
        throws OperationFailedException, PassphraseException {
        checkValidAccount();
        return ethClient.sendTransaction(account, passphrase, publicAddress, amount);
    }

    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return ethClient.getBalance(account);
    }

    @Override
    public Balance getPendingBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return ethClient.getPendingBalance(account);
    }

    void delete(String passphrase) throws DeleteAccountException {
        ethClient.deleteAccount(account, passphrase);
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
}
