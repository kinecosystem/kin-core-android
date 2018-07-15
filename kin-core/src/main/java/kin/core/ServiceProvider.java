package kin.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.responses.AccountResponse;

/**
 * Provides blockchain network details
 */
public class ServiceProvider {

    /**
     * main blockchain network
     */
    public static final String NETWORK_ID_MAIN = "Public Global Kin Ecosystem Network ; June 2018";
    /**
     * test blockchain network
     */
    public static final String NETWORK_ID_TEST = "Kin Playground Network ; June 2018";

    private static final String MAIN_NETWORK_ISSUER = "GDF42M3IPERQCBLWFEZKQRK77JQ65SCKTU3CW36HZVCX7XX5A5QXZIVK";
    private static final String TEST_NETWORK_ISSUER = "GBC3SG6NGTSZ2OMH3FFGB7UVRQWILW367U4GSOOF4TFSZONV42UJXUH7";
    private static final String KIN_ASSET_CODE = "KIN";

    private final String providerUrl;
    private final Network network;
    @Nullable
    private KinAsset kinAsset;

    /**
     * A ServiceProvider used to connect to a horizon network.
     * <p>
     *
     * @param providerUrl the horizon server to use
     * @param networkId the network id, use {@link #NETWORK_ID_MAIN} or {@link #NETWORK_ID_TEST} for public main/testnet
     */
    public ServiceProvider(String providerUrl, String networkId) {
        this.providerUrl = providerUrl;
        this.network = new Network(networkId);
    }

    /**
     * Returns the asset issuer account id, override to provide custom issuer.
     * <p><b>Warning!</b> use for testing only, for testing against custom asset.</p>
     */
    protected String getIssuerAccountId() {
        return isMainNet() ? MAIN_NETWORK_ISSUER : TEST_NETWORK_ISSUER;
    }

    /**
     * Returns the asset code , override to provide custom asset.
     * <p><b>Warning!</b> use for testing only, for testing against custom asset.</p>
     */
    protected String getAssetCode() {
        return KIN_ASSET_CODE;
    }

    final public String getProviderUrl() {
        return providerUrl;
    }

    final public String getNetworkId() {
        return network.getNetworkPassphrase();
    }

    final public boolean isMainNet() {
        return NETWORK_ID_MAIN.equals(network.getNetworkPassphrase());
    }

    final KinAsset getKinAsset() {
        if (kinAsset == null) {
            kinAsset = new KinAsset(getAssetCode(), getIssuerAccountId());
        }
        return kinAsset;
    }

    final Network getNetwork() {
        return network;
    }

    static class KinAsset {

        private final AssetTypeCreditAlphaNum stellarKinAsset;

        KinAsset(String assetCode, String kinIssuerAccountId) {
            KeyPair issuerKeyPair = KeyPair.fromAccountId(kinIssuerAccountId);
            this.stellarKinAsset = (AssetTypeCreditAlphaNum) Asset.createNonNativeAsset(assetCode, issuerKeyPair);
        }

        boolean isKinAsset(@Nullable Asset asset) {
            return asset != null && stellarKinAsset.equals(asset);
        }

        boolean hasKinTrust(@NonNull AccountResponse addresseeAccount) {
            AccountResponse.Balance balances[] = addresseeAccount.getBalances();
            boolean hasTrust = false;
            for (AccountResponse.Balance balance : balances) {
                if (isKinAsset(balance.getAsset())) {
                    hasTrust = true;
                }
            }
            return hasTrust;
        }

        @NonNull
        Asset getStellarAsset() {
            return stellarKinAsset;
        }
    }
}
