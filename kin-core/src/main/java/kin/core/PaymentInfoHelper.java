package kin.core;

import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoText;
import org.stellar.sdk.Operation;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.responses.TransactionResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * Helper class which handle the extraction of Payment Info from a transaction.
 */
public class PaymentInfoHelper {

    public static PaymentInfo extractPaymentInfo(TransactionResponse transactionResponse, Environment.KinAsset kinAsset) {
        PaymentInfo paymentInfo = null;
        List<Operation> operations = transactionResponse.getOperations();
        if (operations != null) {
            for (Operation operation : operations) {
                if (operation instanceof PaymentOperation) {
                    PaymentOperation paymentOperation = (PaymentOperation) operation;
                    if (isPaymentInKin(paymentOperation, kinAsset)) {
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
        return paymentInfo;
    }

    private static String extractSourceAccountId(TransactionResponse transactionResponse, Operation operation) {
        //if payment was sent on behalf of other account - paymentOperation will contains this account, o.w. the source
        //is the transaction source account
        return operation.getSourceAccount() != null ? operation.getSourceAccount()
                .getAccountId() : transactionResponse.getSourceAccount().getAccountId();
    }

    private static boolean isPaymentInKin(PaymentOperation paymentOperation, Environment.KinAsset kinAsset) {
        return kinAsset.isKinAsset(paymentOperation.getAsset());
    }

    private static String extractHashTextIfAny(TransactionResponse transactionResponse) {
        String memoString = null;
        Memo memo = transactionResponse.getMemo();
        if (memo instanceof MemoText) {
            memoString = ((MemoText) memo).getText();
        }
        return memoString;
    }

}
