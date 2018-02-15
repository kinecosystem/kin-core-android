package kin.sdk.core;


import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.M)
class EncryptorImplV23 implements Encryptor {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String ALIAS = "KinKeyStoreAES";
    private static final String JSON_IV = "iv";
    private static final String JSON_CIPHER = "cipher";
    private static final int KEY_SIZE = 128;

    EncryptorImplV23() {
    }

    @Override
    public String encrypt(String secret) throws CryptoException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return aesEncrypt(keyStore, secret);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    @Override
    public String decrypt(String encryptedSecret) throws CryptoException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return aesDecrypt(encryptedSecret, keyStore);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    private String aesDecrypt(String encryptedSecret, KeyStore keyStore) throws Exception {
        JSONObject jsonObject = new JSONObject(encryptedSecret);
        String ivBase64String = jsonObject.getString(JSON_IV);
        String cipherBase64String = jsonObject.getString(JSON_CIPHER);
        byte[] ivBytes = Base64.decode(ivBase64String, Base64.DEFAULT);
        byte[] encryptedSecretBytes = Base64.decode(cipherBase64String, Base64.DEFAULT);

        return performDecryption(keyStore, ivBytes, encryptedSecretBytes);
    }

    @NonNull
    private String performDecryption(KeyStore keyStore, byte[] ivBytes, byte[] encryptedSecretBytes) throws Exception {
        final Cipher cipher = Cipher.getInstance(AES_MODE);
        final GCMParameterSpec spec = new GCMParameterSpec(KEY_SIZE, ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyStore), spec);

        byte[] decryptedBytes = cipher.doFinal(encryptedSecretBytes);
        return new String(decryptedBytes, 0, decryptedBytes.length, "UTF-8");
    }

    private SecretKey getSecretKey(KeyStore keyStore) throws NoSuchAlgorithmException,
        UnrecoverableEntryException, KeyStoreException {
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
    }

    private String aesEncrypt(KeyStore keystore, String secret) throws Exception {
        SecretKey secretKey;
        if (keystore.containsAlias(ALIAS)) {
            secretKey = getSecretKey(keystore);
        } else {
            secretKey = generateAESSecretKey();
        }
        return performEncryption(secretKey, secret.getBytes("UTF-8"));
    }

    private SecretKey generateAESSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(
            new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return keyGenerator.generateKey();
    }

    private String performEncryption(SecretKey secretKey, byte[] secretBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(secretBytes);
        String base64Iv = Base64.encodeToString(cipher.getIV(), Base64.DEFAULT);
        String base64Encrypted = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        return toJson(base64Iv, base64Encrypted);
    }

    private String toJson(String base64Iv, String base64EncyrptedSecret) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_IV, base64Iv);
        jsonObject.put(JSON_CIPHER, base64EncyrptedSecret);
        return jsonObject.toString();
    }

}
