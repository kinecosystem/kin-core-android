package kin.sdk.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import kin.sdk.core.Balance;
import kin.sdk.core.KinAccount;
import kin.sdk.core.ResultCallback;
import kin.sdk.core.exception.DeleteAccountException;
import kin.sdk.core.sample.kin.sdk.core.sample.dialog.KinAlertDialog;

/**
 * Responsible for presenting details about the account
 * Public address, account balance, account balance
 * and in future we will add here button to backup the account (show usage of exportKeyStore)
 * In addition there is "Send Transaction" button here that will navigate to TransactionActivity
 */
public class WalletActivity extends BaseActivity {

    public static final String TAG = WalletActivity.class.getSimpleName();
    public static final String URL_GET_KIN = "http://kin-faucet.rounds.video/send?public_address=";
    private TextView balance, publicKey;
    private View getKinBtn;
    private View balanceProgress;
    private kin.sdk.core.Request<Balance> balanceRequest;

    public static Intent getIntent(Context context) {
        return new Intent(context, WalletActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_activity);
        initWidgets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePublicKey();
        updateBalance(false);
    }

    private void initWidgets() {
        balance = findViewById(R.id.balance);
        publicKey = findViewById(R.id.public_key);

        balanceProgress = findViewById(R.id.balance_progress);

        final View transaction = findViewById(R.id.send_transaction_btn);
        final View refresh = findViewById(R.id.refresh_btn);
        getKinBtn = findViewById(R.id.get_kin_btn);
        final View deleteAccount = findViewById(R.id.delete_account_btn);

        if (isMainNet()) {
            transaction.setBackgroundResource(R.drawable.button_main_network_bg);
            refresh.setBackgroundResource(R.drawable.button_main_network_bg);
            getKinBtn.setVisibility(View.GONE);
        } else {
            getKinBtn.setVisibility(View.VISIBLE);
            getKinBtn.setOnClickListener(view -> {
                getKinBtn.setClickable(false);
                getKin();
            });
        }

        deleteAccount.setOnClickListener(view -> showDeleteAlert());

        transaction.setOnClickListener(view -> startActivity(TransactionActivity.getIntent(WalletActivity.this)));
        refresh.setOnClickListener(view -> updateBalance(true));
    }

    private void showDeleteAlert() {
        KinAlertDialog.createConfirmationDialog(this, getResources().getString(R.string.delete_wallet_warning),
            getResources().getString(R.string.delete), this::deleteAccount).show();
    }

    private void deleteAccount() {
        try {
            getKinClient().deleteAccount(0, getPassphrase());
            onBackPressed();
        } catch (DeleteAccountException e) {
            KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
        }
    }

    private void getKin() {
        final KinAccount account = getKinClient().getAccount(0);
        if (account != null) {
            final String publicAddress = account.getPublicAddress();
            final String url = URL_GET_KIN + publicAddress;
            final RequestQueue queue = Volley.newRequestQueue(this);
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    updateBalance(true);
                    getKinBtn.setClickable(true);
                },
                e -> {
                    KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
                    getKinBtn.setClickable(true);
                });
            stringRequest.setShouldCache(false);
            queue.add(stringRequest);
        }
    }

    private void updatePublicKey() {
        String publicKeyStr = "";
        KinAccount account = getKinClient().getAccount(0);
        if (account != null) {
            publicKeyStr = account.getPublicAddress();
        }
        publicKey.setText(publicKeyStr);
    }

    private void updateBalance(boolean showDialog) {
        balanceProgress.setVisibility(View.VISIBLE);
        KinAccount account = getKinClient().getAccount(0);
        if (account != null) {
            balanceRequest = account.getBalance();
            if (showDialog) {
                balanceRequest.run(new DisplayCallback<Balance>(balanceProgress, balance) {
                    @Override
                    public void displayResult(Context context, View view, Balance result) {
                        ((TextView) view).setText(result.value().toPlainString());
                    }
                });
            } else {
                balanceRequest.run(new ResultCallback<Balance>() {
                    @Override
                    public void onResult(Balance result) {
                        balance.setText(result.value().toPlainString());
                        balanceProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        balance.setText(R.string.balance_error);
                        balanceProgress.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            balance.setText(R.string.balance_error);
        }
    }

    @Override
    Intent getBackIntent() {
        return ChooseNetworkActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.balance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (balanceRequest != null) {
            balanceRequest.cancel(true);
        }
        balance = null;
    }
}
