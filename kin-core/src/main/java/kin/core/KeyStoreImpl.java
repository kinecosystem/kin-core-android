package kin.core;


import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import kin.core.exception.CreateAccountException;
import kin.core.exception.DeleteAccountException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stellar.sdk.KeyPair;

class KeyStoreImpl implements KeyStore {

    static final String ENCRYPTION_VERSION_NAME = "none";
    private static final String STORE_KEY_ACCOUNTS = "accounts";
    private static final String JSON_KEY_ACCOUNTS_ARRAY = "accounts";
    private static final String JSON_KEY_PUBLIC_KEY = "public_key";
    private static final String JSON_KEY_ENCRYPTED_SEED = "seed";
    private static final String VERSION_KEY = "encryptor_ver";

    private final Store store;

    KeyStoreImpl(@NonNull Store store) {
        this.store = store;
    }

    @NonNull
    @Override
    public List<KeyPair> loadAccounts() throws LoadAccountException {
        ArrayList<KeyPair> accounts = new ArrayList<>();
        try {
            JSONArray jsonArray = loadJsonArray();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject accountJson = jsonArray.getJSONObject(i);
                    String seed = accountJson.getString(JSON_KEY_ENCRYPTED_SEED);
                    accounts.add(KeyPair.fromSecretSeed(seed));
                }
            }
        } catch (JSONException e) {
            throw new LoadAccountException(e.getMessage(), e);
        }
        return accounts;
    }

    private JSONArray loadJsonArray() throws JSONException {
        String version = store.getString(VERSION_KEY);
        //ensure current version, drop data if it's a different version
        if (ENCRYPTION_VERSION_NAME.equals(version)) {
            String seedsJson = store.getString(STORE_KEY_ACCOUNTS);
            if (seedsJson != null) {
                JSONObject json = new JSONObject(seedsJson);
                return json.getJSONArray(JSON_KEY_ACCOUNTS_ARRAY);
            }
        } else {
            store.saveString(VERSION_KEY, ENCRYPTION_VERSION_NAME);
        }
        return null;
    }

    @Override
    public void deleteAccount(int index) throws DeleteAccountException {
        JSONObject json = new JSONObject();
        try {
            JSONArray jsonArray = loadJsonArray();
            if (jsonArray != null) {
                JSONArray newJsonArray = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (i != index) {
                        newJsonArray.put(jsonArray.get(i));
                    }
                }
                json.put(JSON_KEY_ACCOUNTS_ARRAY, newJsonArray);
            }
        } catch (JSONException e) {
            throw new DeleteAccountException(e);
        }
        store.saveString(STORE_KEY_ACCOUNTS, json.toString());
    }

    @Override
    public KeyPair newAccount() throws CreateAccountException {
        try {
            KeyPair newKeyPair = KeyPair.random();
            String encryptedSeed = String.valueOf(newKeyPair.getSecretSeed());
            String publicKey = newKeyPair.getAccountId();
            JSONObject accountsJson = addKeyPairToAccounts(encryptedSeed, publicKey);
            store.saveString(STORE_KEY_ACCOUNTS, accountsJson.toString());
            return newKeyPair;
        } catch (JSONException e) {
            throw new CreateAccountException(e);
        }
    }

    private JSONObject addKeyPairToAccounts(@NonNull String encryptedSeed, @NonNull String accountId)
        throws JSONException {
        JSONArray jsonArray = loadJsonArray();
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }

        JSONObject accountJson = new JSONObject();
        accountJson.put(JSON_KEY_ENCRYPTED_SEED, encryptedSeed);
        accountJson.put(JSON_KEY_PUBLIC_KEY, accountId);
        jsonArray.put(accountJson);
        JSONObject json = new JSONObject();
        json.put(JSON_KEY_ACCOUNTS_ARRAY, jsonArray);
        return json;
    }

    @Override
    public void clearAllAccounts() {
        store.clear(STORE_KEY_ACCOUNTS);
    }

}
