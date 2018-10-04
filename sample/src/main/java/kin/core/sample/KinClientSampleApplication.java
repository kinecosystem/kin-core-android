package kin.core.sample;

import android.app.Application;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import kin.core.Environment;
import kin.core.KinClient;

public class KinClientSampleApplication extends Application {

    public enum NetWorkType {
        MAIN,
        TEST
    }

    private KinClient kinClient = null;

    public KinClient createKinClient(NetWorkType type) {
        kinClient = new KinClient.Builder(this)
            .setEnvironment(type == NetWorkType.MAIN ? Environment.PRODUCTION : Environment.TEST)
            .setStoreKey("sample_app")
            .build();
        return kinClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.setVmPolicy(new VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build());
    }

    public KinClient getKinClient() {
        return kinClient;
    }
}
