package kin.sdk.core;

public class ServiceProvider {

    /**
     * main horizon network
     */
    public static final int NETWORK_ID_MAIN = 1;

    /**
     * test horizon network
     */
    public static final int NETWORK_ID_TEST = 2;

    private String providerUrl;
    private int networkId;

    /**
     * A ServiceProvider used to connect to an horizon network.
     * <p>
     * @param providerUrl the provider to use
     * @param networkId for example see {@value #NETWORK_ID_MAIN} {@value NETWORK_ID_TEST}
     */
    public ServiceProvider(String providerUrl, int networkId) {
        this.providerUrl = providerUrl;
        this.networkId = networkId;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public int getNetworkId() {
        return networkId;
    }

    public boolean isMainNet(){
        return networkId == NETWORK_ID_MAIN;
    }
}
