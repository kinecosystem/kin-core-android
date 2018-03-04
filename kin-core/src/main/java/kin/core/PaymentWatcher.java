package kin.core;


import static kin.core.Utils.checkNotNull;

import com.here.oksse.ServerSentEvent;
import java.math.BigDecimal;
import java.util.List;
import kin.core.ServiceProvider.KinAsset;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoText;
import org.stellar.sdk.Operation;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.responses.TransactionResponse;

/**
 * Watch blockchain network for ongoing payments involved specific account.
 * <p>Use {@link #startPaymentListener(WatcherListener)} to start
 * watching payment or {@link #startCreateAccountListener(WatcherListener)} to start
 * watching create account, new payments will be notified on the input {@link WatcherListener WatcherListener&lt;PaymentInfo&gt;}</p>
 */
public class PaymentWatcher {

    private static final String CURSOR_FUTURE_ONLY = "now";
    private final Server server;
    private final KinAsset kinAsset;
    private final KeyPair accountKeyPair;
    private ServerSentEvent serverSentEvent;
    private boolean started;



    PaymentWatcher(Server server, Account account, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
        accountKeyPair = KeyPair.fromAccountId(account.getAccountId());
    }

    /**
     * Start watching for payment, use {@link #stop()} to stop watching, startPaymentListener can be called again after stop.
     * <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    public void startPaymentListener(final WatcherListener<PaymentInfo> listener){
        checkNotNull(listener, "listener");
        setStarted();
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

    /**
     * Start watching for create account, use {@link #stop()} to stop watching, startCreateAccountListener can be called again after stop.
     * <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    public void startCreateAccountListener(final WatcherListener<PaymentInfo> listener){
        checkNotNull(listener, "listener");
        setStarted();
        serverSentEvent = server.transactions()
            .forAccount(accountKeyPair)
            .cursor(CURSOR_FUTURE_ONLY)
            .stream(new EventListener<TransactionResponse>() {
                @Override
                public void onEvent(TransactionResponse transactionResponse) {
                    extractCreateAccountFromTransaction(transactionResponse,listener);
                }
            });
    }

    private void setStarted() {
        if (started) {
            throw new IllegalStateException("Watcher already started");
        }
        started = true;
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
                            extractHashTextIfAny(transactionResponse)
                        );
                        listener.onEvent(paymentInfo);
                    }
                }
            }

        }
    }

    private void extractCreateAccountFromTransaction(TransactionResponse transactionResponse,
        WatcherListener<PaymentInfo> listener) {
        List<Operation> operations = transactionResponse.getOperations();
        if (operations != null) {
            for (Operation operation : operations) {
                if (operation instanceof CreateAccountOperation) {
                    CreateAccountOperation createAccountOperation = (CreateAccountOperation) operation;
                        PaymentInfo paymentInfo = new PaymentInfoImpl(
                            transactionResponse.getCreatedAt(),
                            createAccountOperation.getDestination().getAccountId(),
                            extractSourceAccountId(transactionResponse, createAccountOperation),
                            new BigDecimal(createAccountOperation.getStartingBalance()),
                            new TransactionIdImpl(transactionResponse.getHash()),
                            extractHashTextIfAny(transactionResponse)
                        );
                        listener.onEvent(paymentInfo);
                        break;
                }
            }

        }
    }

    private String extractSourceAccountId(TransactionResponse transactionResponse, Operation operation) {
        //if payment was sent on behalf of other account - paymentOperation will contains this account, o.w. the source
        //is the transaction source account
        return operation.getSourceAccount() != null ? operation.getSourceAccount()
            .getAccountId() : transactionResponse.getSourceAccount().getAccountId();
    }

    private boolean isPaymentInKin(PaymentOperation paymentOperation) {
        return kinAsset.isKinAsset(paymentOperation.getAsset());
    }

    private String extractHashTextIfAny(TransactionResponse transactionResponse) {
        String memoString = null;
        Memo memo = transactionResponse.getMemo();
        if (memo instanceof MemoText) {
            memoString = ((MemoText) memo).getText();
        }
        return memoString;
    }

    /**
     * Stop watching for payments, unregister listener.
     */
    public void stop() {
        if (serverSentEvent != null) {
            serverSentEvent.close();
            started = false;
        }
    }
}
