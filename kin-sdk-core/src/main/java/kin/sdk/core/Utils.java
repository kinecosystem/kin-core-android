package kin.sdk.core;


import java.util.ArrayList;
import kin.sdk.core.exception.TransactionFailedException;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse.Extras.ResultCodes;

final class Utils {

    private Utils() {
        //not instances
    }

    static TransactionFailedException createTransactionException(SubmitTransactionResponse response)
        throws TransactionFailedException {
        ArrayList<String> operationsResultCodes = null;
        String transactionResultCode = null;
        if (response.getExtras() != null && response.getExtras().getResultCodes() != null) {
            ResultCodes resultCodes = response.getExtras().getResultCodes();
            operationsResultCodes = resultCodes.getOperationsResultCodes();
            transactionResultCode = resultCodes.getTransactionResultCode();
        }
        return new TransactionFailedException(transactionResultCode, operationsResultCodes);
    }

    static void checkNotNull(Object obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " == null");
        }
    }
}
