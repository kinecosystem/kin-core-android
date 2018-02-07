package kin.sdk.core;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Store {

    void saveString(@NonNull String key, @NonNull String value);

    @Nullable
    String getString(@NonNull String key);

    void clear(@NonNull String key);
}
