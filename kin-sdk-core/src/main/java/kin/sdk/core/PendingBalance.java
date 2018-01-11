package kin.sdk.core;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.util.KinConverter;
import org.ethereum.geth.Account;
import org.ethereum.geth.Address;
import org.ethereum.geth.Addresses;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.FilterQuery;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Hash;
import org.ethereum.geth.Hashes;
import org.ethereum.geth.Log;
import org.ethereum.geth.Logs;
import org.ethereum.geth.Topics;

/**
 * Calculate pending balance based on current balance, using ethereum contracts events/logs mechanism to iterate all
 * relevant transactions both outgoing and incoming, and summing all transactions amounts.
 */
final class PendingBalance {

    private final EthereumClient ethereumClient;
    private final Context gethContext;
    private final String kinContractAddress;

    PendingBalance(EthereumClient ethereumClient, Context gethContext, String kinContractAddress) {
        this.ethereumClient = ethereumClient;
        this.gethContext = gethContext;
        this.kinContractAddress = kinContractAddress;
    }

    Balance calculate(Account account, Balance balance) throws OperationFailedException {
        try {
            String accountAddressHex = account.getAddress().getHex();

            BigDecimal pendingSpentAmount = getPendingSpentAmount(accountAddressHex);
            BigDecimal pendingEarnAmount = getPendingEarnAmount(accountAddressHex);

            BigDecimal totalPendingAmountInKin = KinConverter.toKin(pendingEarnAmount.subtract(pendingSpentAmount));
            return new BalanceImpl(balance.value().add(totalPendingAmountInKin));
        } catch (Exception e) {
            throw new OperationFailedException(e);
        }
    }

    private BigDecimal getPendingSpentAmount(String accountAddressHex) throws Exception {
        Logs pendingSpentLogs = getPendingTransactionsLogs(accountAddressHex, null);
        return sumTransactionsAmount(pendingSpentLogs);
    }

    private BigDecimal getPendingEarnAmount(String accountAddressHex) throws Exception {
        Logs pendingEarnLogs = getPendingTransactionsLogs(null, accountAddressHex);
        return sumTransactionsAmount(pendingEarnLogs);
    }

    private Logs getPendingTransactionsLogs(@Nullable String fromHexAddress, @Nullable String toHexAddress)
        throws OperationFailedException {
        try {
            Address contractAddress = Geth.newAddressFromHex(kinContractAddress);
            Addresses addresses = Geth.newAddressesEmpty();
            addresses.append(contractAddress);

            Topics topics = createFilterLogTopicsArray(fromHexAddress, toHexAddress);

            FilterQuery filterQuery = new FilterQuery();
            filterQuery.setAddresses(addresses);
            filterQuery.setFromBlock(Geth.newBigInt(Geth.LatestBlockNumber));
            filterQuery.setToBlock(Geth.newBigInt(Geth.PendingBlockNumber));
            filterQuery.setTopics(topics);

            return ethereumClient.filterLogs(gethContext, filterQuery);
        } catch (Exception e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * @param fromHexAddress filter transaction by 'from' hex address, use null for any 'from' address (no filter)
     * @param toHexAddress filter transaction by 'to' hex address, use null for any 'to' address (no filter)
     */
    @NonNull
    private Topics createFilterLogTopicsArray(@Nullable String fromHexAddress, @Nullable String toHexAddress)
        throws Exception {
        //Topics array is 32 bytes array, the first position is topic event signature hash,
        // the rest (up to 3 params) are indexed (= filterable) parameters for the desired event, in our case, transfer indexed params are 'from address' and 'to address'
        // in this order,  param can be 32 bytes hex value representing address, or null, to allow any address (no filter).
        // so if we want to filter by To address only, the first parameter will be null, and the second param will be 32 byte To address.
        // see https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#events
        Topics topics = Geth.newTopicsEmpty();
        Hashes hashes = Geth.newHashesEmpty();

        topics.append(hashes);
        hashes = Geth.newHashesEmpty();
        if (fromHexAddress != null) {
            hashes.append(hexAddressToTopicHash(fromHexAddress));
        }
        topics.append(hashes);
        hashes = Geth.newHashesEmpty();
        if (toHexAddress != null) {
            hashes.append(hexAddressToTopicHash(toHexAddress));
        }
        topics.append(hashes);
        return topics;
    }

    private Hash hexAddressToTopicHash(String hexAddress) throws Exception {
        //add leading zeros to match 32 bytes
        // hex address are 40 chars (20 bytes), topic data is 64 chars (32 bytes)
        return Geth.newHashFromHex("0x000000000000000000000000" + hexAddress.substring(2));
    }

    private BigDecimal sumTransactionsAmount(Logs logs) throws Exception {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < logs.size(); i++) {
            Log log = logs.get(i);
            String txHash = log.getTxHash().getHex();
            if (txHash != null) {
                //getData returns raw data of non-indexed params of event
                //in our case it's the amount param of 'Transfer' event, the format is unsigned int of 32bytes,
                //so it can be converted safely to bigInt
                BigInt txAmount = Geth.newBigInt(0L);
                txAmount.setBytes(log.getData());
                totalAmount = totalAmount.add(new BigDecimal(txAmount.string()));
            }
        }
        return totalAmount;
    }

}
