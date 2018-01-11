package kin.sdk.core;


final class KinConsts {

    public static final String ACCOUNT_SEED = "account_seed";
    public static final String ISSUER_SEED = "issuer_seed";

    private KinConsts() {
    }

    static String getNetworkUrl(ServiceProvider provider) {
        return NetworkConstants.fromProvider(provider).networkUrl;
    }

    /* #enumsmatter */
    enum NetworkConstants {
        NETWORK_MAIN(ServiceProvider.NETWORK_ID_MAIN, "https://horizon.stellar.org"),
        NETWORK_TEST(ServiceProvider.NETWORK_ID_TEST,  "https://horizon-testnet.stellar.org");

        int networkId;
        String networkUrl;

        NetworkConstants(int id, String address){
            networkId = id;
            networkUrl = address;
        }

        // This is used only for testing
        NetworkConstants(int id){
            this(id, System.getProperty("TOKEN_CONTRACT_ADDRESS"));
        }

        static NetworkConstants fromProvider(ServiceProvider provider){
            switch (provider.getNetworkId()) {
                case (ServiceProvider.NETWORK_ID_MAIN) :
                    return NETWORK_MAIN;
                default: return NETWORK_TEST;
            }
        }
    }
}
