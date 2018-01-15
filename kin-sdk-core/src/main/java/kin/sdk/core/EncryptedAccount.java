package kin.sdk.core;


class EncryptedAccount {

    private final String encryptedData;
    private final String accountId;

    EncryptedAccount(String encryptedSeed, String accountId) {
        this.encryptedData = encryptedSeed;
        this.accountId = accountId;
    }

    String getEncryptedData() {
        return encryptedData;
    }

    String getAccountId() {
        return accountId;
    }
}
