package kin.core;


import static kin.core.Utils.checkNotNull;
import static org.stellar.sdk.xdr.OperationType.PAYMENT;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import com.here.oksse.ServerSentEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import kin.core.ServiceProvider.KinAsset;

import org.stellar.sdk.Asset;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.LedgerEntryChange;
import org.stellar.sdk.LedgerEntryChanges;
import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoText;
import org.stellar.sdk.Operation;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.TrustLineLedgerEntryChange;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.TransactionResponse;

/**
 * Provides listeners, for various events happens on the blockchain.
 */
public class BlockchainEvents {

    private static final String CURSOR_FUTURE_ONLY = "now";
    private final Server server;
    private final KinAsset kinAsset;
    private final KeyPair accountKeyPair;

    BlockchainEvents(Server server, String accountId, KinAsset kinAsset) {
        this.server = server;
        this.kinAsset = kinAsset;
        this.accountKeyPair = KeyPair.fromAccountId(accountId);
    }

    /**
     * Creates and adds listener for balance changes of this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    public ListenerRegistration addBalanceListener(@NonNull final EventListener<Balance> listener) {
        checkNotNull(listener, "listener");
        ServerSentEvent serverSentEvent = server
            .transactions()
            .forAccount(accountKeyPair)
            .cursor(CURSOR_FUTURE_ONLY)
            .stream(new org.stellar.sdk.requests.EventListener<TransactionResponse>() {
                @Override
                public void onEvent(TransactionResponse transactionResponse) {
                    extractBalanceChangeFromTransaction(transactionResponse, listener);
                }
            });
        return new ListenerRegistration(serverSentEvent);
    }

    private void extractBalanceChangeFromTransaction(TransactionResponse transactionResponse,
        @NonNull EventListener<Balance> listener) {
        List<LedgerEntryChanges> ledgerChanges = transactionResponse.getLedgerChanges();
        if (ledgerChanges != null) {
            for (LedgerEntryChanges ledgerChange : ledgerChanges) {
                LedgerEntryChange[] ledgerEntryUpdates = ledgerChange.getLedgerEntryUpdates();
                if (ledgerEntryUpdates != null) {
                    for (LedgerEntryChange ledgerEntryUpdate : ledgerEntryUpdates) {
                        extractBalanceFromTrustLineUpdate(listener, ledgerEntryUpdate);
                    }
                }
            }
        }
    }

    private void extractBalanceFromTrustLineUpdate(@NonNull EventListener<Balance> listener,
        LedgerEntryChange ledgerEntryUpdate) {
        if (ledgerEntryUpdate instanceof TrustLineLedgerEntryChange) {
            TrustLineLedgerEntryChange trustLineUpdate = (TrustLineLedgerEntryChange) ledgerEntryUpdate;
            KeyPair account = trustLineUpdate.getAccount();
            if (account != null) {
                if (accountKeyPair.getAccountId().equals(account.getAccountId())
                    && kinAsset.isKinAsset(trustLineUpdate.getAsset())) {
                    BalanceImpl balance = new BalanceImpl(
                        new BigDecimal(trustLineUpdate.getBalance()));
                    listener.onEvent(balance);
                }
            }
        }
    }

    /**
     * Creates and adds listener for payments concerning this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    public ListenerRegistration addPaymentListener(@NonNull final EventListener<PaymentInfo> listener) {
        checkNotNull(listener, "listener");
        ServerSentEvent serverSentEvent = server
            .transactions()
            .forAccount(accountKeyPair)
            .cursor(CURSOR_FUTURE_ONLY)
            .stream(new org.stellar.sdk.requests.EventListener<TransactionResponse>() {
                @Override
                public void onEvent(TransactionResponse transactionResponse) {
                    extractPaymentsFromTransaction(transactionResponse, listener);
                }
            });
        return new ListenerRegistration(serverSentEvent);
    }

    /**
     * Creates and adds listener for account creation event, use returned {@link ListenerRegistration} to stop
     * listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    public ListenerRegistration addAccountCreationListener(final EventListener<Void> listener) {
        checkNotNull(listener, "listener");
        ServerSentEvent serverSentEvent = server.transactions()
            .forAccount(accountKeyPair)
            .stream(new org.stellar.sdk.requests.EventListener<TransactionResponse>() {

                private boolean eventOccurred = false;

                @Override
                public void onEvent(TransactionResponse transactionResponse) {
                    //account creation is one time operation, fire event only once
                    if (!eventOccurred) {
                        eventOccurred = true;
                        listener.onEvent(null);
                    }
                }
            });
        return new ListenerRegistration(serverSentEvent);
    }

    private void extractPaymentsFromTransaction(TransactionResponse transactionResponse,
                                                EventListener<PaymentInfo> listener) {
        PaymentInfo paymentInfo = getPaymentInfo(transactionResponse);
        if (paymentInfo != null) { // TODO: 11/10/2018 did it in this way so i could extract to method the other parts
            listener.onEvent(paymentInfo);
        }
    }

    PaymentInfo getPaymentInfo(TransactionResponse transactionResponse) {
        PaymentInfo paymentInfo = null;
        List<Operation> operations = transactionResponse.getOperations();
        if (operations != null) {
            for (Operation operation : operations) {
                if (operation instanceof PaymentOperation) {
                    PaymentOperation paymentOperation = (PaymentOperation) operation;

                    // TODO: 11/10/2018 after looking on Ron code i saw that he looked at more conditions than only this
//                  # A transaction will not be simplified if:
//                  # 1. It contains a memo that is not a text memo
//                  # 2. It contains multiple operations
//                  # 3. It contains a payment that is not of KIN/XLM
//                  # 4. It contains activation to anything other than KIN
//                  # 5. Its operation type is not one of 'Payment'/'Activation'/'Create account'.
                    if (isPaymentInKin(paymentOperation)) {
                        paymentInfo = new PaymentInfoImpl(
                                transactionResponse.getCreatedAt(),
                                paymentOperation.getDestination().getAccountId(),
                                extractSourceAccountId(transactionResponse, paymentOperation),
                                new BigDecimal(paymentOperation.getAmount()),
                                new TransactionIdImpl(transactionResponse.getHash()),
                                extractHashTextIfAny(transactionResponse)
                        );
                    }
                }
            }
        }
        // return a new payment info object if possible, otherwise return null.
        return paymentInfo;
    }

    private String extractSourceAccountId(TransactionResponse transactionResponse, org.stellar.sdk.Operation operation) {
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

}
