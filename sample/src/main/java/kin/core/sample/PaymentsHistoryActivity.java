package kin.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.Button;

import kin.core.PaymentsHistoryRequestParams;
import kin.sdk.core.sample.R;

public class PaymentsHistoryActivity extends BaseActivity {

    public static final String TAG = PaymentsHistoryActivity.class.getSimpleName();
    public static final String EXTRA_TRANSACTION_HISTORY_PARAMS = "extraTransactionHistoryParams";

    private TextInputLayout accountId;
    private TextInputLayout cursorText;
    private TextInputLayout limitText;
    private Button ascButton;
    private Button descButton;

    public static Intent getIntent(Context context) {
        return new Intent(context, PaymentsHistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        initUiViews();
    }

    @Override
    Intent getBackIntent() {
        return WalletActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.transaction_history;
    }

    private void initUiViews() {
        accountId = findViewById(R.id.public_address);
        cursorText = findViewById(R.id.cursor);
        limitText = findViewById(R.id.limit);
        ascButton = findViewById(R.id.asc);
        descButton = findViewById(R.id.desc);

        ascButton.setOnClickListener(v -> {
            descButton.setSelected(false);
            v.setSelected(true);
        });

        descButton.setOnClickListener(v -> {
            ascButton.setSelected(false);
            v.setSelected(true);
        });

        findViewById(R.id.get_transaction_history_btn).setOnClickListener(v -> {
            PaymentsHistoryRequestParams paymentsHistoryRequestParams = buildTransactionParams();
            Intent intent = ShowPaymentsHistoryActivity.getIntent(PaymentsHistoryActivity.this);
            intent.putExtra(EXTRA_TRANSACTION_HISTORY_PARAMS, paymentsHistoryRequestParams);
            startActivity(intent);
        });
    }

    private PaymentsHistoryRequestParams buildTransactionParams() {
        PaymentsHistoryRequestParams.PaymentsHistoryRequestParamsBuilder builder = new PaymentsHistoryRequestParams.PaymentsHistoryRequestParamsBuilder();
        String publicAddress = accountId.getEditText().getText().toString();
        if (!TextUtils.isEmpty(publicAddress)) {
            builder.account(publicAddress);
        }
        String cursor = cursorText.getEditText().getText().toString();
        if (!TextUtils.isEmpty(cursor)) {
            builder.cursor(cursor);
        }
        String limit = limitText.getEditText().getText().toString();
        if (!TextUtils.isEmpty(limit)) {
            builder.limit(Integer.valueOf(limit));
        }

        if (ascButton.isSelected()) {
            builder.order(PaymentsHistoryRequestParams.Order.ASC);
        } else if (descButton.isSelected()) {
            builder.order(PaymentsHistoryRequestParams.Order.DESC);
        }
        return builder.build();
    }


}
