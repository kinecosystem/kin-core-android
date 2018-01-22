package kin.sdk.core;


final class KinConsts {

    private KinConsts() {
    }

    static final String ABI = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_newOwnerCandidate\",\"type\":\"address\"}],\"name\":\"requestOwnershipTransfer\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"isMinting\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_amount\",\"type\":\"uint256\"}],\"name\":\"mint\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"balance\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"acceptOwnership\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"newOwnerCandidate\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_tokenAddress\",\"type\":\"address\"},{\"name\":\"_amount\",\"type\":\"uint256\"}],\"name\":\"transferAnyERC20Token\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"},{\"name\":\"_spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"remaining\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"endMinting\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[],\"name\":\"MintingEnded\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_by\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"}],\"name\":\"OwnershipRequested\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"}]";

    /**
     * <p>SHA3 (Keccak-256) hash of ERC-20's Transfer event:</p>
     * {@code Transfer(address,address,uint256)}
     */
    static final String TOPIC_EVENT_NAME_SHA3_TRANSFER = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    static long getTransferKinGasLimit(ServiceProvider provider) {
        return NetworkConstants.fromProvider(provider).transferKinGasLimit;
    }

    static String getContractAddress(ServiceProvider provider) {
        return NetworkConstants.fromProvider(provider).contractAddress;
    }

    /* #enumsmatter */
    enum NetworkConstants {
        NETWORK_MAIN(ServiceProvider.NETWORK_ID_MAIN, "0x818fc6c2ec5986bc6e2cbf00939d90556ab12ce5"),
        NETWORK_ROPSTEN(ServiceProvider.NETWORK_ID_ROPSTEN,  "0xEF2Fcc998847DB203DEa15fC49d0872C7614910C"),
        NETWORK_RINKEBY(ServiceProvider.NETWORK_ID_RINKEBY,  "0xEF2Fcc998847DB203DEa15fC49d0872C7614910C"),
        NETWORK_TRUFFLE(ServiceProvider.NETWORK_ID_TRUFFLE);

        int networkId;
        String contractAddress;
        long transferKinGasLimit;

        // gas limit for 'kin transfer'
        private static final long DEFAULT_TRANSFER_KIN_GAS_LIMIT = 60000;

        NetworkConstants(int id, String address){
            networkId = id;
            contractAddress = address;
            transferKinGasLimit = DEFAULT_TRANSFER_KIN_GAS_LIMIT;
        }

        // This is used only for testing
        NetworkConstants(int id){
            this(id, System.getProperty("TOKEN_CONTRACT_ADDRESS"));
        }

        static NetworkConstants fromProvider(ServiceProvider provider){
            switch (provider.getNetworkId()) {
                case (ServiceProvider.NETWORK_ID_MAIN) :
                    return NETWORK_MAIN;
                case (ServiceProvider.NETWORK_ID_RINKEBY) :
                    return NETWORK_RINKEBY;
                case (ServiceProvider.NETWORK_ID_TRUFFLE) :
                    return NETWORK_TRUFFLE;
                default: return NETWORK_ROPSTEN;
            }
        }
    }
}
