package kin.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.Button;

import kin.core.TransactionHistoryRequestParams;
import kin.sdk.core.sample.R;

public class TransactionHistoryActivity extends BaseActivity {

    public static final String TAG = TransactionHistoryActivity.class.getSimpleName();
    public static final String EXTRA_TRANSACTION_HISTORY_PARAMS = "extraTransactionHistoryParams";

    private TextInputLayout accountId;
    private TextInputLayout cursorText;
    private TextInputLayout limitText;
    private Button ascButton;
    private Button descButton;

    public static Intent getIntent(Context context) {
        return new Intent(context, TransactionHistoryActivity.class);
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
            TransactionHistoryRequestParams transactionHistoryRequestParams = buildTransactionParams();
            Intent intent = ShowTransactionHistoryActivity.getIntent(TransactionHistoryActivity.this);
            intent.putExtra(EXTRA_TRANSACTION_HISTORY_PARAMS, transactionHistoryRequestParams);
            startActivity(intent);
        });
    }

    private TransactionHistoryRequestParams buildTransactionParams() {
        TransactionHistoryRequestParams.TransactionHistoryRequestParamsBuilder builder = new TransactionHistoryRequestParams.TransactionHistoryRequestParamsBuilder();
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
            builder.order(TransactionHistoryRequestParams.Order.ASC);
        } else if (descButton.isSelected()) {
            builder.order(TransactionHistoryRequestParams.Order.DESC);
        }
        return builder.build();
    }


}
