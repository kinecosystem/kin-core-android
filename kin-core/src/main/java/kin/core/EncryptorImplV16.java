package kin.core;


/**
 * UNSAFE no-op encryptor for below api 18, it will do no encryption
 */
class EncryptorImplV16 implements Encryptor {

    @Override
    public String encrypt(String secret) throws CryptoException {
        return secret;
    }

    @Override
    public String decrypt(String encryptedSecret) throws CryptoException {
        return encryptedSecret;
    }
}
