package kin.sdk.core;

import org.ethereum.geth.Account;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Signer;
import org.ethereum.geth.Transaction;

import kin.sdk.core.exception.PassphraseException;

/**
 * Responsible for signing transactions with passphrase.
 */
class KinSigner implements Signer {

    private Account from;
    private KeyStore keyStore;
    private String passphrase;
    private BigInt networkId;

    KinSigner(Account from, KeyStore keyStore, String passphrase, int networkId) {
        this.from = from;
        this.keyStore = keyStore;
        this.passphrase = passphrase;
        this.networkId = Geth.newBigInt(networkId);
    }

    @Override
    public Transaction sign(Address address, Transaction transaction) throws Exception {
        try {
            transaction = keyStore.signTxPassphrase(from, passphrase, transaction, networkId);
        } catch (Exception e) {
            throw new PassphraseException();
        }
        return transaction;
    }
}
