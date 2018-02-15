package kin.core;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressLint("NewApi")
class EncryptorFactory {

    private static final String VERSION_KEY = "encryptor_ver";

    //no instances
    private EncryptorFactory() {
    }

    @TargetApi(VERSION_CODES.M)
    static Encryptor create(Context context, Store store) {
        String versionString = store.getString(VERSION_KEY);

        Encryptor encryptor = createEncryptorAccordingToSavedVersion(context, versionString, store);
        if (encryptor == null) {
            encryptor = createEncryptorByAndroidVersion(context, store);
        }

        return encryptor;
    }

    @Nullable
    private static Encryptor createEncryptorAccordingToSavedVersion(Context context, String versionString,
        Store store) {
        //handle android version upgrades, keystore version will be saved first time keystore is created,
        //after upgrade we'll fallback to old encyrptor version if already exists, o.w. older keys cannot be restored
        if (String.valueOf(VERSION_CODES.M).equals(versionString)) {
            return createVersion23(store);
        } else if (String.valueOf(VERSION_CODES.JELLY_BEAN_MR2).equals(versionString)) {
            return createVersion18(context, store);
        } else if (String.valueOf(VERSION_CODES.JELLY_BEAN).equals(versionString)) {
            return createVersion16(store);
        }
        return null;
    }

    @NonNull
    private static Encryptor createEncryptorByAndroidVersion(Context context, Store store) {
        //at fresh start, encryptor will be created according Android version
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            return createVersion23(store);
        } else if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            return createVersion18(context, store);
        } else {
            return createVersion16(store);
        }
    }

    @NonNull
    private static Encryptor createVersion16(Store store) {
        store.saveString(VERSION_KEY, String.valueOf(VERSION_CODES.JELLY_BEAN));
        return new EncryptorImplV16();
    }

    @NonNull
    private static Encryptor createVersion18(Context context, Store store) {
        store.saveString(VERSION_KEY, String.valueOf(VERSION_CODES.JELLY_BEAN_MR2));
        return new EncryptorImplV18(context.getApplicationContext());
    }

    @NonNull
    private static Encryptor createVersion23(Store store) {
        store.saveString(VERSION_KEY, String.valueOf(VERSION_CODES.M));
        return new EncryptorImplV23();
    }
}
