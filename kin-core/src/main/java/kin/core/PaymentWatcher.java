package kin.core;


import static kin.core.Utils.checkNotNull;

import android.support.annotation.NonNull;
import java.math.BigDecimal;
import java.util.List;
import kin.core.ServiceProvider.KinAsset;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoHash;
import org.stellar.sdk.Operation;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.sse.ServerSentEvent;

/**
 * Watch blockchain network for ongoing payments involved specific account.
 * <p>Use {@link #start(WatcherListener)} to start
 * watching, new payments will be notified on the input {@link WatcherListener WatcherListener&lt;PaymentInfo&gt;}</p>
 */
public class PaymentWatcher {

    private static final String CURSOR_FUTURE_ONLY = "now";
    private final Server server;
    private final KinAsset kinAsset;
    private final KeyPair accountKeyPair;
    private boolean started;
    private ServerSentEvent serverSentEvent;

    PaymentWatcher(Server server, Account account, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
        accountKeyPair = KeyPair.fromAccountId(account.getAccountId());
    }

    /**
     * Start watching for payments, use {@link #stop()} to stop watching.
     * <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    public void start(@NonNull final WatcherListener<PaymentInfo> listener) {
        checkNotNull(listener, "listener");
        if (started) {
            throw new IllegalStateException("Watcher already started");
        }
        started = true;
        serverSentEvent = server
            .transactions()
            .forAccount(accountKeyPair)
            .cursor(CURSOR_FUTURE_ONLY)
            .stream(new EventListener<TransactionResponse>() {
                @Override
                public void onEvent(TransactionResponse transactionResponse) {
                    extractPaymentsFromTransaction(transactionResponse, listener);
                }
            });
    }

    private void extractPaymentsFromTransaction(TransactionResponse transactionResponse,
        WatcherListener<PaymentInfo> listener) {
        List<Operation> operations = transactionResponse.getOperations();
        if (operations != null) {
            for (Operation operation : operations) {
                if (operation instanceof PaymentOperation) {
                    PaymentOperation paymentOperation = (PaymentOperation) operation;
                    if (isPaymentInKin(paymentOperation)) {
                        PaymentInfo paymentInfo = new PaymentInfoImpl(
                            transactionResponse.getCreatedAt(),
                            paymentOperation.getDestination().getAccountId(),
                            extractSourceAccountId(transactionResponse, paymentOperation),
                            new BigDecimal(paymentOperation.getAmount()),
                            new TransactionIdImpl(transactionResponse.getHash()),
                            extractHashMemoIfAny(transactionResponse)
                        );
                        listener.onEvent(paymentInfo);
                    }
                }
            }

        }
    }

    private String extractSourceAccountId(TransactionResponse transactionResponse, PaymentOperation paymentOperation) {
        //if payment was sent on behalf of other account - paymentOperation will contains this account, o.w. the source
        //is the transaction source account
        return paymentOperation.getSourceAccount() != null ? paymentOperation.getSourceAccount()
            .getAccountId() : transactionResponse.getSourceAccount().getAccountId();
    }

    private boolean isPaymentInKin(PaymentOperation paymentOperation) {
        return paymentOperation.getAsset() != null && kinAsset.isKinAsset(paymentOperation.getAsset());
    }

    private byte[] extractHashMemoIfAny(TransactionResponse transactionResponse) {
        byte[] memoBytes = null;
        Memo memo = transactionResponse.getMemo();
        if (memo instanceof MemoHash) {
            memoBytes = ((MemoHash) memo).getBytes();
        }
        return memoBytes;
    }

    /**
     * Stop watching for payments
     */
    public void stop() {
        serverSentEvent.close();
    }
}
