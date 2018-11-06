package kin.sdk.sample;

import android.app.Application;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import kin.sdk.Environment;
import kin.sdk.KinClient;

public class KinClientSampleApplication extends Application {

    public enum NetWorkType {
        MAIN,
        TEST
    }

    private KinClient kinClient = null;

    public KinClient createKinClient(NetWorkType type, String appId) {
        kinClient = new KinClient(this, type == NetWorkType.MAIN ? Environment.PRODUCTION : Environment.TEST, appId, "sample_app");
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
