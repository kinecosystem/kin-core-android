package kin.sdk.core;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public class BaseTest {

    /**
     * The Computer localhost on Android Studio emulator is 10.0.2.2
     * The Computer localhost on Genymotion emulator is 10.0.3.2
     */
    private static final String TESTRPC_PROVIDER_URL = "http://10.0.2.2:8545";
    private static final int TESTRPC_NETWORK_ID = 9 ; //truffle network id

    private Context context;
    private ServiceProvider serviceProvider;
    KinClient kinClient;
    Config config;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getContext();
        serviceProvider = new ServiceProvider(TESTRPC_PROVIDER_URL, TESTRPC_NETWORK_ID);
        getConfigFile();
        clearKeyStore();
        kinClient = new KinClient(context, serviceProvider);
    }


    private void getConfigFile() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("testConfig.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        config = new Gson().fromJson(json, Config.class);
        // Set environment var of token contract address, to be used in ClientWrapper;
        System.setProperty("TOKEN_CONTRACT_ADDRESS", config.getContractAddress());
    }

    @After
    public void tearDown() throws Exception {
        clearKeyStore();
    }

    private void clearKeyStore() {
        // Removes the previews KeyStore if exists
        String networkId = String.valueOf(serviceProvider.getNetworkId());
        String keyStorePath = new StringBuilder(context.getFilesDir().getAbsolutePath())
            .append(File.separator)
            .append("kin")
            .append(File.separator)
            .append("keystore")
            .append(File.separator)
            .append(networkId).toString();

        File keystoreDir = new File(keyStorePath);
        FileUtils.deleteRecursive(keystoreDir);
    }
}
