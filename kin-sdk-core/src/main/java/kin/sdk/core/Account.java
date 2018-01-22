package kin.sdk.core;


class Account {

    private final String encryptedSeed;
    private final String accountId;

    Account(String encryptedSeed, String accountId) {
        this.encryptedSeed = encryptedSeed;
        this.accountId = accountId;
    }

    /**
     * Save secrete seed encrypted, decrypt it on demand only using the KeyStore
     */
    String getEncryptedSeed() {
        return encryptedSeed;
    }

    /**
     * return account id (public key)
     */
    String getAccountId() {
        return accountId;
    }
}
