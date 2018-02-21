package kin.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import kin.core.KinAccount;
import kin.core.KinClient;
import kin.core.PaymentInfo;
import kin.core.PaymentWatcher;
import kin.sdk.core.sample.R;

public class WatchPaymentActivity extends BaseActivity {

    public static final String TAG = WatchPaymentActivity.class.getSimpleName();
    private ViewGroup rootView;
    private PaymentWatcher paymentWatcher;
    private KinAccount account;
    private View startBtn;
    private View stopBtn;

    public static Intent getIntent(Context context) {
        return new Intent(context, WatchPaymentActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_payments_activity);
        initWidgets();
        KinClient kinClient = ((KinClientSampleApplication) getApplication()).getKinClient();
        account = kinClient.getAccount(0);
    }

    private void initWidgets() {
        rootView = findViewById(R.id.payments_container);
        stopBtn = findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(view -> stopWatcher());
        startBtn = findViewById(R.id.start_btn);
        startBtn.setOnClickListener(view -> startWatcher());
    }

    private void startWatcher() {
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        paymentWatcher = account.createPaymentWatcher();
        paymentWatcher.start(paymentInfo -> runOnUiThread(() -> addPaymentToUi(paymentInfo)));
    }

    private void addPaymentToUi(PaymentInfo paymentInfo) {
        View paymentView = LayoutInflater.from(this).inflate(R.layout.payment_info, null);
        TextView destinationText = paymentView.findViewById(R.id.to_public_id);
        TextView sourceText = paymentView.findViewById(R.id.from_public_id);
        TextView amountText = paymentView.findViewById(R.id.amount);
        TextView memoText = paymentView.findViewById(R.id.memo);
        TextView hashText = paymentView.findViewById(R.id.tx_hash);
        TextView createdAtText = paymentView.findViewById(R.id.created_at);

        destinationText.setText(paymentInfo.destinationPublicKey());
        sourceText.setText(paymentInfo.sourcePublicKey());
        amountText.setText(paymentInfo.amount().toPlainString());
        hashText.setText(paymentInfo.hash().id());
        memoText.setText(paymentInfo.memo());
        createdAtText.setText(paymentInfo.createdAt());

        rootView.addView(paymentView, 0);
    }

    private void stopWatcher() {
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        paymentWatcher.stop();
    }

    @Override
    Intent getBackIntent() {
        return WalletActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.watch_payments;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentWatcher != null) {
            paymentWatcher.stop();
        }
    }
}
