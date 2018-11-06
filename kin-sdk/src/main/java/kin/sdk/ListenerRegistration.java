package kin.sdk;


import com.here.oksse.ServerSentEvent;

/**
 * Represents a listener to {@link BlockchainEvents}, that can be removed using {@link #remove()}.
 */
public class ListenerRegistration {

    private final ServerSentEvent serverSentEvent;

    ListenerRegistration(ServerSentEvent serverSentEvent) {
        this.serverSentEvent = serverSentEvent;
    }

    /**
     * Remove and unregisters this listener.
     */
    public void remove() {
        serverSentEvent.close();
    }
}
