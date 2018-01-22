package kin.sdk.core.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import kin.sdk.core.KinClient;

public abstract class BaseActivity extends AppCompatActivity {

    // ideally user should be asked for a passphrase when
    // creating an account and then the same passphrase
    // should be used when sending transactions
    // To make the UI simpler for the sample application
    // we are using a hardcoded passphrase.
    final static String PASSPHRASE1 = "12345";
    final static int NO_ACTION_BAR_TITLE = -1;

    abstract Intent getBackIntent();

    abstract int getActionBarTitleRes();

    public boolean isMainNet() {
        if (getKinClient() != null && getKinClient().getServiceProvider() != null) {
            return getKinClient().getServiceProvider().isMainNet();
        }
        return false;
    }

    protected boolean hasBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = isMainNet() ? R.style.AppTheme_Main : R.style.AppTheme_Test;
        setTheme(theme);
        initActionBar();
    }

    private void initActionBar() {
        if (getActionBarTitleRes() != NO_ACTION_BAR_TITLE) {
            getSupportActionBar().setTitle(getActionBarTitleRes());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(hasBack());
    }

    @Override
    public void onBackPressed() {
        Intent intent = getBackIntent();
        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }
        finish();
    }

    public KinClient getKinClient() {
        KinClientSampleApplication application = (KinClientSampleApplication) getApplication();
        return application.getKinClient();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        finish();
    }

    public String getPassphrase() {
        return PASSPHRASE1;
    }

    protected void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
