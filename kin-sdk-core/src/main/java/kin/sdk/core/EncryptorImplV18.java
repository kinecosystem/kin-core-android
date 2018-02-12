package kin.sdk.core;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

class EncryptorImplV18 implements Encryptor {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    private static final String ALIAS = "KinKeyStoreRSA";
    private static final String RSA = "RSA";

    private final Context context;

    EncryptorImplV18(Context context) {
        this.context = context;
    }

    public String encrypt(String secret) throws CryptoException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return rsaEncrypt(keyStore, secret);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public String decrypt(String encryptedSecret) throws CryptoException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return rsaDecrypt(encryptedSecret, keyStore);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    private String rsaEncrypt(KeyStore keyStore, String secret) throws Exception {
        Entry entry = keyStore.getEntry(ALIAS, null);
        PublicKey publicKey;
        if (entry == null) {
            KeyPair rsaKeys = generateRsaPair();
            publicKey = rsaKeys.getPublic();
        } else {
            publicKey = ((PrivateKeyEntry) entry).getCertificate().getPublicKey();
        }
        byte[] encryptedBytes = performEncryption(secret.getBytes("UTF-8"), publicKey);
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
    private KeyPair generateRsaPair()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
            .setAlias(ALIAS)
            .setSubject(new X500Principal("CN=" + ALIAS))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.getTime())
            .setEndDate(end.getTime())
            .build();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA, ANDROID_KEY_STORE);
        kpg.initialize(spec);
        return kpg.generateKeyPair();
    }

    private byte[] performEncryption(byte[] secretBytes, PublicKey publicKey)
        throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IOException {
        Cipher input = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER);
        input.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(
            outputStream, input);
        cipherOutputStream.write(secretBytes);
        cipherOutputStream.close();
        return outputStream.toByteArray();
    }

    private String rsaDecrypt(String encryptedSecret64, KeyStore keyStore) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);

        Cipher output = Cipher.getInstance(RSA_MODE);
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        byte[] encryptedSecretBytes = Base64.decode(encryptedSecret64, Base64.DEFAULT);
        CipherInputStream cipherInputStream = new CipherInputStream(
            new ByteArrayInputStream(encryptedSecretBytes), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }
        return new String(bytes, 0, bytes.length, "UTF-8");
    }
}
