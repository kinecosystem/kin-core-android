package kin.core.sample;

import android.app.Application;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import kin.core.KinClient;
import kin.core.ServiceProvider;

public class KinClientSampleApplication extends Application {

    private final String TEST_NET_URL = "https://horizon-kik.kininfrastructure.com";
    private final String MAIN_NET_URL = "https://horizon.stellar.org";
    private final String TEST_NET_NETWORK_ID = "private testnet";

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
                netWorkId = TEST_NET_NETWORK_ID;
                break;
            default:
                providerUrl = TEST_NET_URL;
                netWorkId = TEST_NET_NETWORK_ID;
        }
        kinClient = new KinClient(this,
            new ServiceProvider(providerUrl, netWorkId) {
                @Override
                protected String getIssuerAccountId() {
                    return "GBQ3DQOA7NF52FVV7ES3CR3ZMHUEY4LTHDAQKDTO6S546JCLFPEQGCPK";
                }
            });
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
