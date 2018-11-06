package kin.sdk;


import static kin.sdk.Utils.checkNotNull;

import android.support.annotation.NonNull;
import com.here.oksse.ServerSentEvent;
import java.math.BigDecimal;
import java.util.List;
import kin.sdk.Environment.KinAsset;
import kin.base.KeyPair;
import kin.base.LedgerEntryChange;
import kin.base.LedgerEntryChanges;
import kin.base.Memo;
import kin.base.MemoText;
import kin.base.Operation;
import kin.base.PaymentOperation;
import kin.base.Server;
import kin.base.TrustLineLedgerEntryChange;
import kin.base.responses.TransactionResponse;

/**
 * Provides listeners, for various events happens on the blockchain.
 */
class BlockchainEvents {

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
    ListenerRegistration addBalanceListener(@NonNull final EventListener<Balance> listener) {
        checkNotNull(listener, "listener");
        ServerSentEvent serverSentEvent = server
            .transactions()
            .forAccount(accountKeyPair)
            .cursor(CURSOR_FUTURE_ONLY)
            .stream(new kin.base.requests.EventListener<TransactionResponse>() {
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
    ListenerRegistration addPaymentListener(@NonNull final EventListener<PaymentInfo> listener) {
        checkNotNull(listener, "listener");
        ServerSentEvent serverSentEvent = server
            .transactions()
            .forAccount(accountKeyPair)
            .cursor(CURSOR_FUTURE_ONLY)
            .stream(new kin.base.requests.EventListener<TransactionResponse>() {
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
    ListenerRegistration addAccountCreationListener(final EventListener<Void> listener) {
        checkNotNull(listener, "listener");
        ServerSentEvent serverSentEvent = server.transactions()
            .forAccount(accountKeyPair)
            .stream(new kin.base.requests.EventListener<TransactionResponse>() {

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
}
