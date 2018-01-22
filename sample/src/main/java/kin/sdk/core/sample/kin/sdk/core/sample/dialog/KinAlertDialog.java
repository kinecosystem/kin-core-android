package kin.sdk.core.sample.kin.sdk.core.sample.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import kin.sdk.core.sample.R;
import kin.sdk.core.sample.Utils;

public class KinAlertDialog {

    private Context context;
    private android.app.AlertDialog dialog;
    private OnConfirmedListener confirmedListener;
    private String positiveButtonText;

    public static KinAlertDialog createErrorDialog(Context context, String message) {
        KinAlertDialog dialog = new KinAlertDialog(context);
        dialog.setMessage(message);
        dialog.setConfirmButton();
        return dialog;
    }

    public static KinAlertDialog createConfirmationDialog(Context context, String message, String confirmationText,
        OnConfirmedListener confirmedListener) {
        KinAlertDialog dialog = new KinAlertDialog(context);
        dialog.setPositiveButtonText(confirmationText);
        dialog.setOnConfirmedListener(confirmedListener);
        dialog.setMessage(message);
        dialog.setConfirmButton();
        dialog.setCancelButton();
        return dialog;
    }

    private KinAlertDialog(Context context) {
        this.context = context;
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        dialog = builder.create();
        dialog.setCancelable(true);
        positiveButtonText = context.getResources().getString(R.string.ok);
    }

    public void show() {
        if (context != null && !((Activity) context).isFinishing()) {
            dialog.show();
        }
    }

    private void setPositiveButtonText(String text){
        if(TextUtils.isEmpty(text)) {
            positiveButtonText = context.getResources().getString(R.string.ok);
        }else {
            positiveButtonText = text;
        }
    }

    protected void setMessage(String message) {
        if(TextUtils.isEmpty(message)){
            message = context.getResources().getString(R.string.error_no_message);
        }
        dialog.setView(buildMessageView(message));
    }

    protected void setOnConfirmedListener(OnConfirmedListener onConfirmedListener){
        this.confirmedListener = onConfirmedListener;
    }

    protected void setConfirmButton() {
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText,
            (dialogInterface, i) -> {
                dialogInterface.dismiss();
                onConfirmed();
            });
    }

    protected void setCancelButton() {
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel),
            (dialogInterface, i) -> dialogInterface.dismiss());
    }

    protected void onConfirmed() {
        if (confirmedListener != null) {
            confirmedListener.onConfirm();
        }
    }

    private View buildMessageView(String message) {
        TextView textView = new TextView(context);
        textView.setTextColor(R.drawable.text_color);
        textView.setTextIsSelectable(true);
        textView.setTextSize(18f);
        textView.setText(message);
        textView.setGravity(Gravity.LEFT);
        textView.setPadding(35, 35, 35, 0);
        textView.setOnLongClickListener(v -> {
            Utils.copyToClipboard(v.getContext(), message);
            Toast.makeText(v.getContext(), R.string.copied_to_clipboard,
                Toast.LENGTH_SHORT)
                .show();
            return true;
        });
        return textView;
    }
}
