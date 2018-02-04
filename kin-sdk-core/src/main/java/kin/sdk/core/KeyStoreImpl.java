package kin.sdk.core;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stellar.sdk.KeyPair;

class KeyStoreImpl implements KeyStore {

    private static final String PREF_NAME = "KinKeyStore";
    private static final String PREF_KEY_SECRET_SEEDS = "secret_seed";
    private static final String JSON_SEEDS = "seeds";

    private final SharedPreferences sharedPref;

    KeyStoreImpl(Context context) {
        this.sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public List<Account> loadAccounts() {
        JSONArray jsonArray = loadJsonArray();
        ArrayList<Account> accounts = new ArrayList<>();
        if (jsonArray != null) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String seed = (String) jsonArray.get(i);
                    KeyPair keyPair = KeyPair.fromSecretSeed(seed);
                    accounts.add(new Account(String.valueOf(keyPair.getSecretSeed()), keyPair.getAccountId()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return accounts;
    }

    private JSONArray loadJsonArray() {
        String seedsJson = sharedPref.getString(PREF_KEY_SECRET_SEEDS, null);
        if (seedsJson != null) {
            try {
                JSONObject json = new JSONObject(seedsJson);
                return json.getJSONArray(JSON_SEEDS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void deleteAccount(int index, String passphrase) {
        JSONArray jsonArray = loadJsonArray();
        if (jsonArray != null) {
            JSONObject json = new JSONObject();
            try {
                JSONArray newJsonArray = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (i != index) {
                        newJsonArray.put(jsonArray.get(i));
                    }
                }
                json.put(JSON_SEEDS, newJsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sharedPref.edit()
                .putString(PREF_KEY_SECRET_SEEDS, json.toString())
                .apply();
        }
    }

    @Override
    public Account newAccount(String passphrase) {
        KeyPair newAccount = KeyPair.random();
        addKeyPair(newAccount, passphrase);
        return new Account(String.valueOf(newAccount.getSecretSeed()), newAccount.getAccountId());
    }

    private void addKeyPair(@NonNull KeyPair keyPair, @NonNull String passphrase) {
        JSONArray jsonArray = loadJsonArray();
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        jsonArray.put(String.valueOf(keyPair.getSecretSeed()));
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_SEEDS, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sharedPref.edit()
            .putString(PREF_KEY_SECRET_SEEDS, json.toString())
            .apply();
    }

    @Nullable
    @Override
    public String exportAccount(@NonNull Account account, @NonNull String passphrase) {
        return account.getEncryptedSeed();
    }

    @Override
    public KeyPair decryptAccount(Account account, String passphrase) {
        return KeyPair.fromSecretSeed(account.getEncryptedSeed());
    }

    @Override
    public void clearAllAccounts() {
        sharedPref.edit().clear().apply();
    }
}
