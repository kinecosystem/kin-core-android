package kin.sdk;


import kin.sdk.Environment.KinAsset;
import kin.base.Server;

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
