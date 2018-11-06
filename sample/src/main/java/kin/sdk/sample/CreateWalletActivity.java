package kin.sdk.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import kin.sdk.KinClient;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.core.sample.R;

public class CreateWalletActivity extends BaseActivity {

    public static final String TAG = CreateWalletActivity.class.getSimpleName();

    public static Intent getIntent(Context context) {
        return new Intent(context, CreateWalletActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_wallet_activity);
        initWidgets();
    }

    private void initWidgets() {
        View createWallet = findViewById(R.id.btn_create_wallet);
        View importWallet = findViewById(R.id.btn_import_wallet);
        if (isMainNet()) {
            createWallet.setBackgroundResource(R.drawable.button_main_network_bg);
        }
        createWallet.setOnClickListener(view -> createAccount());
        importWallet.setOnClickListener(view -> showImportDialog());
    }

    private void createAccount() {
        try {
            final KinClient kinClient = getKinClient();
            kinClient.addAccount();
            startActivity(WalletActivity.getIntent(this));
        } catch (CreateAccountException e) {
            Utils.logError(e, "createAccount");
            KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
        }
    }

    private void showImportDialog() {

        @SuppressLint("InflateParams") View content = LayoutInflater.from(this).inflate(R.layout.import_wallet, null);
        final EditText editPassphrase = content.findViewById(R.id.passphrase);
        final EditText editExportedJson = content.findViewById(R.id.exported_json);

        final AlertDialog dialog = new Builder(CreateWalletActivity.this)
            .setView(content)
            .setTitle("Import Wallet Json")
            .setPositiveButton("Import", (dialog1, which) ->
                importAccount(editExportedJson.getText().toString(), editPassphrase.getText().toString()))
            .setNegativeButton("Cancel", null)
            .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(editExportedJson.getText().toString()) ||
                    TextUtils.isEmpty(editPassphrase.getText().toString())) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        };
        editExportedJson.addTextChangedListener(textWatcher);
        editPassphrase.addTextChangedListener(textWatcher);

        dialog.show();
    }

    private void importAccount(String json, String passphrase) {
        try {
            final KinClient kinClient = getKinClient();
            kinClient.importAccount(json, passphrase);
            startActivity(WalletActivity.getIntent(this));
        } catch (Exception e) {
            Utils.logError(e, "importAccount");
            KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
        }
    }

    @Override
    Intent getBackIntent() {
        return ChooseNetworkActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.create_wallet;
    }

}
