package kin.core;


import android.support.annotation.NonNull;
import java.util.ArrayList;
import kin.core.exception.TransactionFailedException;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse.Extras.ResultCodes;

final class Utils {

    private Utils() {
        //no instances
    }

    static TransactionFailedException createTransactionException(@NonNull SubmitTransactionResponse response)
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

    static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static void checkNotNull(Object obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " == null");
        }
    }

    static void checkNotEmpty(String string, String paramName) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty.");
        }
    }
}
