package kin.sdk.core;

public class ServiceProvider {

    /**
     * main ethereum network
     */
    public static final int NETWORK_ID_MAIN = 1;

    /**
     * ropsten ethereum TEST network
     */
    public static final int NETWORK_ID_ROPSTEN = 3;

    /**
     * rinkeby ethereum TEST network
     */
    public static final int NETWORK_ID_RINKEBY = 4;

    /**
     * truffle testrpc network
     */
    public static final int NETWORK_ID_TRUFFLE = 9;


    private String providerUrl;
    private int networkId;

    /**
     * A ServiceProvider used to connect to an ethereum node.
     * <p>
     * For example to connect to an infura test node use
     * new ServiceProvider("https://ropsten.infura.io/YOURTOKEN", NETWORK_ID_ROPSTEN);
     *
     * @param providerUrl the provider to use
     * @param networkId for example see {@value #NETWORK_ID_MAIN} {@value NETWORK_ID_ROPSTEN} {@value
     * NETWORK_ID_RINKEBY}
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
