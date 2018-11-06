package kin.sdk;


import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class SharedPrefStore implements Store {

    private final SharedPreferences sharedPref;

    SharedPrefStore(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    @Override
    public void saveString(@NonNull String key, @NonNull String value) {
        sharedPref.edit()
            .putString(key, value)
            .apply();
    }

    @Override
    @Nullable
    public String getString(@NonNull String key) {
        return sharedPref.getString(key, null);
    }

    @Override
    public void clear(@NonNull String key) {
        sharedPref.edit().remove(key).apply();
    }
}
