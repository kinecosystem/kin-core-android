package kin.sdk.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import kin.sdk.core.KinAccount;
import kin.sdk.core.exception.AccountDeletedException;
import kin.sdk.core.exception.OperationFailedException;
import kin.sdk.core.exception.PassphraseException;
import kin.sdk.core.sample.kin.sdk.core.sample.dialog.KinAlertDialog;
import org.json.JSONException;

/**
 * Enter passphrase to generate Json content that can be used to access the current account
 */
public class ExportKeystoreActivity extends BaseActivity {

    public static final String TAG = ExportKeystoreActivity.class.getSimpleName();

    public static Intent getIntent(Context context) {
        return new Intent(context, ExportKeystoreActivity.class);
    }

    private View exportBtn, copyBtn;
    private EditText passphraseInput;
    private TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_key_store_activity);
        initWidgets();
    }

    private void initWidgets() {
        copyBtn = findViewById(R.id.copy_btn);
        exportBtn = findViewById(R.id.generate_btn);
        passphraseInput = (EditText) findViewById(R.id.passphrase_input);
        outputTextView = (TextView) findViewById(R.id.output);

        findViewById(R.id.copy_btn).setOnClickListener(view -> {
            selectAll();
            Utils.copyToClipboard(this, outputTextView.getText());
        });

        if (isMainNet()) {
            exportBtn.setBackgroundResource(R.drawable.button_main_network_bg);
            copyBtn.setBackgroundResource(R.drawable.button_main_network_bg);
        }
        passphraseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    clearOutput();
                    if (!exportBtn.isEnabled()) {
                        exportBtn.setEnabled(true);
                    }
                } else if (exportBtn.isEnabled()) {
                    exportBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passphraseInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(view);
            }
        });

        exportBtn.setOnClickListener(view -> {
            exportBtn.setEnabled(false);
            hideKeyboard(exportBtn);
            try {
                String privateKeyString = generatePrivateKeyStoreString();
                updateOutput(privateKeyString);
                copyBtn.setEnabled(true);
            } catch (PassphraseException e) {
                clearAll();
                KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
            } catch (JSONException e) {
                clearAll();
                KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
            } catch (OperationFailedException e) {
                clearAll();
                KinAlertDialog.createErrorDialog(this, e.getMessage()).show();
            }
        });
    }

    private void clearAll() {
        clearOutput();
        passphraseInput.setText("");
    }

    private void selectAll() {
        outputTextView.setSelectAllOnFocus(true);
        outputTextView.clearFocus();
        outputTextView.requestFocus();
        outputTextView.setSelectAllOnFocus(false);
    }

    private String generatePrivateKeyStoreString()
        throws PassphraseException, JSONException, OperationFailedException {
        KinAccount account = getKinClient().getAccount();
        if (account == null) {
            throw new AccountDeletedException();
        }
        return account.exportKeyStore(getPassphrase(), passphraseInput.getText().toString());
    }

    private void updateOutput(String outputString) {
        if (TextUtils.isEmpty(outputString)) {
            outputTextView.setText(outputString);
            outputTextView.setTextIsSelectable(false);
        } else {
            outputTextView.setText(outputString);
            outputTextView.setTextIsSelectable(true);
            outputTextView.requestFocus();
        }
    }

    private void clearOutput() {
        updateOutput(null);
        copyBtn.setEnabled(false);
    }

    @Override
    Intent getBackIntent() {
        return WalletActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.export_key_store;
    }
}
