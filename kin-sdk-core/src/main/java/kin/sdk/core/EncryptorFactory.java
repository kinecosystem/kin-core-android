package kin.sdk.core;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;

class EncryptorFactory {

    private static final String VERSION_KEY = "encryptor_ver";
    private static final String VER_23 = "23";
    private static final String VER_18 = "18";
    private static final String VER_16 = "16";

    //no instances
    private EncryptorFactory() {
    }

    static Encryptor create(Context context, Store store) {
        String versionString = store.getString(VERSION_KEY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (VER_18.equals(versionString)) {
                store.saveString(VERSION_KEY, VER_18);
                return new EncryptorImplV18(context.getApplicationContext());
            } else if (VER_16.equals(versionString)) {
                store.saveString(VERSION_KEY, VER_16);
                return new EncryptorImplV16();
            }
            store.saveString(VERSION_KEY, "23");
            return new EncryptorImplV23();
        } else if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            if (VER_16.equals(versionString)) {
                store.saveString(VERSION_KEY, VER_16);
                return new EncryptorImplV16();
            }
            store.saveString(VERSION_KEY, VER_18);
            return new EncryptorImplV18(context.getApplicationContext());
        } else {
            store.saveString(VERSION_KEY, "16");
            return new EncryptorImplV16();
        }
    }
}
