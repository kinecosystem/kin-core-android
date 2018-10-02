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
public class Environment {

    private static final String KIN_ASSET_CODE = "KIN";

    public static final Environment PRODUCTION =
        new Environment("https://horizon-ecosystem.kininfrastructure.com",
            "Public Global Kin Ecosystem Network ; June 2018",
            "GDF42M3IPERQCBLWFEZKQRK77JQ65SCKTU3CW36HZVCX7XX5A5QXZIVK",
            KIN_ASSET_CODE);

    public static final Environment TEST =
        new Environment("https://horizon-playground.kininfrastructure.com",
            "Kin Playground Network ; June 2018",
            "GBC3SG6NGTSZ2OMH3FFGB7UVRQWILW367U4GSOOF4TFSZONV42UJXUH7",
            KIN_ASSET_CODE);

    private final String networkUrl;
    private final Network network;

    private final String issuerAccountId;

    private final String assetCode;
    @Nullable
    private KinAsset kinAsset;

    private Environment(String networkUrl, String networkPassphrase, String issuerAccountId, String assetCode) {
        this.networkUrl = networkUrl;
        this.network = new Network(networkPassphrase);
        this.issuerAccountId = issuerAccountId;
        this.assetCode = assetCode;
    }

    /**
     * Returns the URL of the blockchain node.
     */
    final public String getNetworkUrl() {
        return networkUrl;
    }

    /**
     * Returns the network id.
     */
    final public String getNetworkPassphrase() {
        return network.getNetworkPassphrase();
    }

    /**
     * Returns the KIN issuer account ID.
     */
    final public String getIssuerAccountId() {
        return issuerAccountId;
    }

    /**
     * Returns the asset code, default is "KIN".
     */
    final public String getAssetCode() {
        return assetCode;
    }

    final public boolean isMainNet() {
        return PRODUCTION.getNetworkPassphrase().equals(network.getNetworkPassphrase());
    }

    final KinAsset getKinAsset() {
        if (kinAsset == null) {
            kinAsset = new KinAsset(assetCode, issuerAccountId);
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

    public static class Builder {

        private String networkUrl;
        private String networkPassphrase;
        private String issuerAccountId;
        private String assetCode = KIN_ASSET_CODE;

        /**
         * The URL of the blockchain node.
         */
        public Builder networkUrl(String networkUrl) {
            this.networkUrl = networkUrl;
            return this;
        }

        /**
         * The network id to be used.
         */
        public Builder networkPassphrase(String networkPassphrase) {
            this.networkPassphrase = networkPassphrase;
            return this;
        }

        /**
         * The asset issuer account ID.
         */
        public Builder issuerAccountId(String issuerAccountId) {
            this.issuerAccountId = issuerAccountId;
            return this;
        }

        /**
         * The asset code, optional, the default is "KIN".
         * <p><b>Warning!</b> use for testing only, for testing against custom asset.</p>
         */
        public Builder assetCode(String assetCode) {
            this.assetCode = assetCode;
            return this;
        }

        /**
         * Build an Environment object.
         */
        public Environment build() {
            Utils.checkNotEmpty(networkUrl, "networkUrl");
            Utils.checkNotEmpty(networkPassphrase, "networkPassphrase");
            Utils.checkNotEmpty(issuerAccountId, "issuerAccountId");
            Utils.checkNotEmpty(assetCode, "assetCode");

            return new Environment(networkUrl, networkPassphrase, issuerAccountId, assetCode);
        }
    }
}
