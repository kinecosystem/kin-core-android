package kin.sdk.core;

import java.math.BigDecimal;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import org.stellar.sdk.KeyPair;


final class KinAccountImpl extends AbstractKinAccount {

    private ClientWrapper ethClient;
    private KeyPair account;
    private boolean isDeleted;

    /**
     * Creates a new {@link KeyPair}.
     *
     * @param clientWrapper that will be use to call to Kin smart-contract.
     * @param passphrase that will be used to store the account private key securely.
     * @throws Exception if go-ethereum was unable to generate the account (unable to generate new key or store the
     * key).
     */
    KinAccountImpl(ClientWrapper clientWrapper, String passphrase) throws Exception {
        this.account = KeyPair.random();
        this.ethClient = clientWrapper;
        this.ethClient.saveSeed(KinConsts.ACCOUNT_SEED, account.getSecretSeed());
        if(this.ethClient.getServiceProvider().isMainNet()) {
            this.ethClient.createAccountAndGetXLM(account);
        }
        isDeleted = false;
    }

    /**
     * Creates a {@link KinAccount} from existing {@link KeyPair}
     *
     * @param clientWrapper that will be use to call to Kin smart-contract.
     * @param account the existing Account.
     */
    KinAccountImpl(ClientWrapper clientWrapper, KeyPair account) {
        this.account = account;
        this.ethClient = clientWrapper;
        isDeleted = false;
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
        return new String(account.getSecretSeed());
    }

    @Override
    public TransactionId sendTransactionSync(String publicAddress, String passphrase, BigDecimal amount)
        throws OperationFailedException, PassphraseException {
        checkValidAccount();
        return ethClient.sendTransaction(account, publicAddress, amount);
    }

    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return ethClient.getBalance(account);
    }

    @Override
    public Balance getPendingBalanceSync() throws OperationFailedException {
        checkValidAccount();
        //TODO remove or change !?@
        return ethClient.getBalance(account);
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
