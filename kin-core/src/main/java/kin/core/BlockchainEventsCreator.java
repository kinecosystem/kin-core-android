package kin.core;


import kin.core.Environment.KinAsset;
import org.stellar.sdk.Server;

class BlockchainEventsCreator {

    private final Server server;
    private final KinAsset kinAsset;


    BlockchainEventsCreator(Server server, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
    }

    BlockchainEvents create(String accountId) {
        return new BlockchainEvents(server, accountId, kinAsset);
    }
}
