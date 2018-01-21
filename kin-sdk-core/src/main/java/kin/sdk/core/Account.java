package kin.sdk.core;


class Account {

    private final String encryptedData;
    private final String accountId;

    Account(String encryptedSeed, String accountId) {
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
