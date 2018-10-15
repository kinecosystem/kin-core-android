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
    static final String EXTRA_LIMIT = "extraLimit";
    static final String EXTRA_ORDER_BY = "extraOrderBy";

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
            // This is just a simple example of how to do it with params.
            // because PaymentsHistoryRequestParams is not Parcelable then we add each value to the intent as an extra.
            Intent intent = ShowPaymentsHistoryActivity.getIntent(PaymentsHistoryActivity.this);
            PaymentsHistoryRequestParams params = buildPaymentsParams();
            intent = buildIntentWithPaymentsParams(intent, params);
            startActivity(intent);
        });
    }

    private PaymentsHistoryRequestParams buildPaymentsParams() {
        PaymentsHistoryRequestParams.Builder builder = new PaymentsHistoryRequestParams.Builder();
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

    private Intent buildIntentWithPaymentsParams(Intent intent, PaymentsHistoryRequestParams params) {
        if (params.getLimit() > 0) {
            intent.putExtra(EXTRA_LIMIT, params.getLimit());
        }
        if (params.getOrder() != null) {
            intent.putExtra(EXTRA_ORDER_BY, params.getOrder().name());
        }
        return intent;
    }


}
