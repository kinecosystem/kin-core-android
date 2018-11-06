package kin.sdk.sample;

import android.content.Context;
import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

    public static void copyToClipboard(Context context, CharSequence textToCopy) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
            clipboard.setText(textToCopy);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                .newPlainText("copied text", textToCopy);
            clipboard.setPrimaryClip(clip);
        }
    }

    public static void logError(Throwable t, String operationName) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        Log.e("KinSampleApp", operationName + "error = " + sw.toString());
    }
}
