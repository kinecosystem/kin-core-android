package kin.core.sample;

import android.app.Application;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import kin.core.KinClient;
import kin.core.ServiceProvider;

public class KinClientSampleApplication extends Application {

    private final static String TEST_NET_URL = "https://horizon-playground.kininfrastructure.com/";
    private final static String MAIN_NET_URL = "https://horizon-ecosystem.kininfrastructure.com/";

    public enum NetWorkType {
        MAIN,
        TEST
    }

    private KinClient kinClient = null;

    public KinClient createKinClient(NetWorkType type) {
        String providerUrl;
        String netWorkId;
        switch (type) {
            case MAIN:
                providerUrl = MAIN_NET_URL;
                netWorkId = ServiceProvider.NETWORK_ID_MAIN;
                break;
            case TEST:
                providerUrl = TEST_NET_URL;
                netWorkId = ServiceProvider.NETWORK_ID_TEST;
                break;
            default:
                providerUrl = TEST_NET_URL;
                netWorkId = ServiceProvider.NETWORK_ID_TEST;
        }
        kinClient = new KinClient(this,
            new ServiceProvider(providerUrl, netWorkId), "sample_app");
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
