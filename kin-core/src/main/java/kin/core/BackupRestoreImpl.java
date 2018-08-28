package kin.core;

import android.support.annotation.NonNull;
import java.io.UnsupportedEncodingException;
import kin.core.exception.CryptoException;
import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;
import org.stellar.sdk.KeyPair;

class BackupRestoreImpl implements BackupRestore {

    private static final String JSON_KEY_PUBLIC_KEY = "pkey";
    private static final String JSON_KEY_SEED = "seed";
    private static final String JSON_KEY_SALT = "salt";
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int HASH_LENGTH_BYTES = 32;
    private static final int OUPUT_JSON_INDENT_SPACES = 2;

    BackupRestoreImpl() {
        //init sodium
        NaCl.sodium();
    }

    @Override
    @NonNull
    public String exportWallet(@NonNull KeyPair keyPair, @NonNull String passphrase)
        throws CryptoException {
        byte[] saltBytes = generateRandomBytes(SALT_LENGTH_BYTES);

        byte[] passphraseBytes = stringToUTF8ByteArray(passphrase);
        byte[] hash = keyHash(passphraseBytes, saltBytes);
        byte[] secretSeedBytes = keyPair.getRawSecretSeed();

        byte[] encryptedSeed = encryptSecretSeed(hash, secretSeedBytes);

        String salt = bytesToHex(saltBytes);
        String seed = bytesToHex(encryptedSeed);
        return jsonify(keyPair.getAccountId(), salt, seed);
    }

    @Override
    @NonNull
    public KeyPair importWallet(@NonNull String exportedJson, @NonNull String passphrase) throws CryptoException {
        AccountJson accountJson = stringify(exportedJson);

        byte[] passphraseBytes = stringToUTF8ByteArray(passphrase);
        byte[] saltBytes = hexStringToByteArray(accountJson.getSaltHex());
        byte[] keyHash = keyHash(passphraseBytes, saltBytes);
        byte[] seedBytes = hexStringToByteArray(accountJson.getSeedHex());

        byte[] decryptedBytes = decryptSecretSeed(seedBytes, keyHash);
        return KeyPair.fromSecretSeed(decryptedBytes);
    }

    private byte[] generateRandomBytes(int len) {
        byte[] randomBuffer = new byte[len];
        Sodium.randombytes_buf(randomBuffer, len);
        return randomBuffer;
    }

    private byte[] encryptSecretSeed(byte[] hash, byte[] secretSeedBytes) throws CryptoException {
        byte[] cipherText = new byte[secretSeedBytes.length + Sodium.crypto_secretbox_macbytes()];
        byte[] nonceBytes = generateRandomBytes(Sodium.crypto_secretbox_noncebytes());

        if (Sodium.crypto_secretbox_easy(cipherText, secretSeedBytes, secretSeedBytes.length, nonceBytes, hash) != 0) {
            throw new CryptoException("Encrypting data failed.");
        }
        byte[] encryptedSeed = new byte[cipherText.length + nonceBytes.length];
        System.arraycopy(nonceBytes, 0, encryptedSeed, 0, nonceBytes.length);
        System.arraycopy(cipherText, 0, encryptedSeed, nonceBytes.length, cipherText.length);
        return encryptedSeed;
    }

    private byte[] decryptSecretSeed(byte[] seedBytes, byte[] keyHash) throws CryptoException {
        byte[] nonceBytes = new byte[Sodium.crypto_secretbox_noncebytes()];
        byte[] cipherBytes = new byte[seedBytes.length - nonceBytes.length];
        System.arraycopy(seedBytes, 0, nonceBytes, 0, nonceBytes.length);
        System.arraycopy(seedBytes, nonceBytes.length, cipherBytes, 0, cipherBytes.length);

        byte[] decryptedBytes = new byte[cipherBytes.length - Sodium.crypto_secretbox_macbytes()];
        if (Sodium.crypto_secretbox_open_easy(decryptedBytes, cipherBytes, cipherBytes.length, nonceBytes, keyHash)
            != 0) {
            throw new CryptoException("Decrypting data failed.");
        }
        return decryptedBytes;
    }

    private String jsonify(String publicAddress, String salt, String seed) throws CryptoException {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_KEY_PUBLIC_KEY, publicAddress);
            json.put(JSON_KEY_SEED, seed);
            json.put(JSON_KEY_SALT, salt);
            return json.toString(OUPUT_JSON_INDENT_SPACES);
        } catch (JSONException e) {
            throw new CryptoException("Json exception", e);
        }
    }

    private AccountJson stringify(String exportedJson) throws CryptoException {
        try {
            JSONObject json = new JSONObject(exportedJson);
            String seedHex = json.getString(JSON_KEY_SEED);
            String saltHex = json.getString(JSON_KEY_SALT);
            return new AccountJson(seedHex, saltHex);
        } catch (JSONException e) {
            throw new CryptoException(e);
        }
    }

    private byte[] keyHash(byte[] passphraseBytes, byte[] saltBytes) throws CryptoException {
        int pwhashAlgoId = Sodium.crypto_pwhash_alg_default();
        byte[] hash = new byte[HASH_LENGTH_BYTES];
        if (Sodium.crypto_pwhash(hash, HASH_LENGTH_BYTES, passphraseBytes, passphraseBytes.length, saltBytes,
            Sodium.crypto_pwhash_opslimit_interactive(), Sodium.crypto_pwhash_memlimit_interactive(), pwhashAlgoId)
            != 0) {
            throw new CryptoException("Generating hash failed.");
        }
        return hash;
    }

    private byte[] stringToUTF8ByteArray(String s) throws CryptoException {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CryptoException(e);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private class AccountJson {

        private final String seedHex;
        private final String saltHex;

        AccountJson(String seedHex, String saltHex) {
            this.seedHex = seedHex;
            this.saltHex = saltHex;
        }


        String getSeedHex() {
            return seedHex;
        }

        String getSaltHex() {
            return saltHex;
        }

    }
}