package kin.core;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import java.lang.annotation.Retention;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.AccountResponse;

/**
 * Provides blockchain network details
 */
public class ServiceProvider {

    /**
     * main blockchain network
     */
    public static final int NETWORK_ID_MAIN = 1;
    /**
     * test blockchain network
     */
    public static final int NETWORK_ID_TEST = 2;

    private static final String MAIN_NETWORK_ISSUER = "GBGFNADX2FTYVCLDCVFY5ZRTVEMS4LV6HKMWOY7XJKVXMBIWVDESCJW5";
    private static final String TEST_NETWORK_ISSUER = "GCKG5WGBIJP74UDNRIRDFGENNIH5Y3KBI5IHREFAJKV4MQXLELT7EX6V";

    private final String providerUrl;
    @NetworkId
    private final int networkId;
    private final KinAsset kinAsset;

    @Retention(SOURCE)
    @IntDef({NETWORK_ID_MAIN, NETWORK_ID_TEST})
    public @interface NetworkId {

    }

    /**
     * A ServiceProvider used to connect to a horizon network.
     * <p>
     *
     * @param providerUrl the horizon server to use
     * @param networkId either {@link #NETWORK_ID_MAIN} or {@link #NETWORK_ID_TEST}
     */
    public ServiceProvider(String providerUrl, @NetworkId int networkId) {
        this.providerUrl = providerUrl;
        this.networkId = networkId;
        this.kinAsset = new KinAsset(isMainNet() ? MAIN_NETWORK_ISSUER : TEST_NETWORK_ISSUER);
    }

    @VisibleForTesting
    ServiceProvider(String providerUrl, String issuerAccountId) {
        this.providerUrl = providerUrl;
        this.networkId = NETWORK_ID_TEST;
        this.kinAsset = new KinAsset(issuerAccountId);
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public int getNetworkId() {
        return networkId;
    }

    public boolean isMainNet() {
        return networkId == NETWORK_ID_MAIN;
    }

    KinAsset getKinAsset() {
        return kinAsset;
    }

    static class KinAsset {

        private static final String KIN_ASSET_CODE = "KIN";
        private final AssetTypeCreditAlphaNum stellarKinAsset;

        KinAsset(String kinIssuerAccountId) {
            KeyPair issuerKeyPair = KeyPair.fromAccountId(kinIssuerAccountId);
            this.stellarKinAsset = (AssetTypeCreditAlphaNum) Asset.createNonNativeAsset(KIN_ASSET_CODE, issuerKeyPair);
        }

        boolean isKinBalance(@NonNull AccountResponse.Balance balance) {
            return stellarKinAsset.getCode().equals(balance.getAssetCode()) &&
                balance.getAsset() != null &&
                isKinAsset(balance.getAsset());
        }

        boolean isKinAsset(@NonNull Asset asset) {
            return stellarKinAsset.equals(asset);
        }

        boolean hasKinTrust(@NonNull AccountResponse addresseeAccount) {
            AccountResponse.Balance balances[] = addresseeAccount.getBalances();
            boolean hasTrust = false;
            for (AccountResponse.Balance balance : balances) {
                if (isKinBalance(balance)) {
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
