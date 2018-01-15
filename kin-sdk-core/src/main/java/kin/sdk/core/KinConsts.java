package kin.sdk.core;


import org.stellar.sdk.KeyPair;

final class KinConsts {

    private KinConsts() {
    }

    static final String KIN_ASSET_CODE = "KIN";

    static KeyPair getKinIssuer(ServiceProvider provider) {
        return NetworkConstants.fromProvider(provider).issuer;
    }

    /* #enumsmatter */
    private enum NetworkConstants {
        NETWORK_MAIN(ServiceProvider.NETWORK_ID_MAIN, "GBGFNADX2FTYVCLDCVFY5ZRTVEMS4LV6HKMWOY7XJKVXMBIWVDESCJW5"),
        NETWORK_TEST(ServiceProvider.NETWORK_ID_TEST, "GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM");

        final int networkId;
        final KeyPair issuer;

        NetworkConstants(int id, String issuerAccountId) {
            networkId = id;
            this.issuer = KeyPair.fromAccountId(issuerAccountId);
        }

        static NetworkConstants fromProvider(ServiceProvider provider) {
            switch (provider.getNetworkId()) {
                case (ServiceProvider.NETWORK_ID_MAIN):
                    return NETWORK_MAIN;
                default:
                    return NETWORK_TEST;
            }
        }
    }
}
