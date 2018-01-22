package kin.sdk.core;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Config {

    @SerializedName("accounts")
    private List<EcdsaAccount> accounts;

    @SerializedName("token_contract_address")
    private String contractAddress;

    public List<EcdsaAccount> getAccounts() {
        return accounts;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public class EcdsaAccount {

        @SerializedName("private_key")
        private String key;

        public String getKey() {
            return key;
        }
    }
}
