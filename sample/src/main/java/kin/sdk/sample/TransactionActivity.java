package kin.sdk.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import java.math.BigDecimal;
import kin.sdk.KinAccount;
import kin.sdk.Request;
import kin.sdk.ResultCallback;
import kin.sdk.Transaction;
import kin.sdk.TransactionId;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.core.sample.R;

/**
 * Displays form to enter public address and amount and a button to send a transaction
 */
public class TransactionActivity extends BaseActivity {

    public static final String TAG = TransactionActivity.class.getSimpleName();

    public static Intent getIntent(Context context) {
        return new Intent(context, TransactionActivity.class);
    }

    private View sendTransaction, progressBar;

    private EditText toAddressInput, amountInput, memoInput;
    private Request<Transaction> buildTransactionRequest;
    private Request<TransactionId> sendTransactionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_activity);
        initWidgets();
    }

    private void initWidgets() {
        sendTransaction = findViewById(R.id.send_transaction_btn);
        progressBar = findViewById(R.id.transaction_progress);
        toAddressInput = findViewById(R.id.to_address_input);
        amountInput = findViewById(R.id.amount_input);
        memoInput = findViewById(R.id.memo_input);

        if (getKinClient().getEnvironment().isMainNet()) {
            sendTransaction.setBackgroundResource(R.drawable.button_main_network_bg);
        }
        toAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(amountInput.getText())) {
                    if (!sendTransaction.isEnabled()) {
                        sendTransaction.setEnabled(true);
                    }
                } else if (sendTransaction.isEnabled()) {
                    sendTransaction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(toAddressInput.getText())) {
                    if (!sendTransaction.isEnabled()) {
                        sendTransaction.setEnabled(true);
                    }
                } else if (sendTransaction.isEnabled()) {
                    sendTransaction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        toAddressInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && !toAddressInput.hasFocus()) {
                hideKeyboard(view);
            }
        });

        amountInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && !amountInput.hasFocus()) {
                hideKeyboard(view);
            }
        });

        sendTransaction.setOnClickListener(view -> {
            BigDecimal amount = new BigDecimal(amountInput.getText().toString());
            try {
                sendTransaction(toAddressInput.getText().toString(), amount, memoInput.getText().toString());
            } catch (OperationFailedException e) {
                Utils.logError(e, "sendTransaction");
                KinAlertDialog.createErrorDialog(TransactionActivity.this, e.getMessage()).show();
            }
        });
    }

    @Override
    Intent getBackIntent() {
        return WalletActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.transaction;
    }

    private void sendTransaction(String toAddress, BigDecimal amount, String memo) throws OperationFailedException {
        progressBar.setVisibility(View.VISIBLE);
        KinAccount account = getKinClient().getAccount(0);
        if (account != null) {
            DisplayCallback<TransactionId> callback = new DisplayCallback<TransactionId>(progressBar) {
                @Override
                public void displayResult(Context context, View view, TransactionId transactionId) {
                    KinAlertDialog.createErrorDialog(context, "Transaction id " + transactionId.id()).show();
                }
            };
            if (memo == null) {
                buildTransactionRequest = account.buildTransaction(toAddress, amount);

            } else {
                buildTransactionRequest = account.buildTransaction(toAddress, amount, memo);
            }
            buildTransactionRequest.run(new ResultCallback<Transaction>() {
                @Override
                public void onResult(Transaction transaction) {
                    Log.d(TAG, "sendTransaction: build transaction " + transaction.getId().id() + " succeeded");
                    sendTransactionRequest = account.sendTransaction(transaction);
                    sendTransactionRequest.run(callback);
                }

                @Override
                public void onError(Exception e) {
                    Utils.logError(e, "sendTransaction");
                    KinAlertDialog.createErrorDialog(TransactionActivity.this, e.getMessage()).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            throw new AccountDeletedException();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buildTransactionRequest != null) {
            buildTransactionRequest.cancel(false);
        }
        if (sendTransactionRequest != null) {
            sendTransactionRequest.cancel(false);
        }
        progressBar = null;
    }
}
