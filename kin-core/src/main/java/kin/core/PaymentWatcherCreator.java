package kin.core;


import kin.core.ServiceProvider.KinAsset;
import org.stellar.sdk.Server;

class PaymentWatcherCreator {

    private final Server server;
    private final KinAsset kinAsset;


    PaymentWatcherCreator(Server server, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
    }

    PaymentWatcher create(Account account) {
        return new PaymentWatcher(server, account, kinAsset);
    }
}
