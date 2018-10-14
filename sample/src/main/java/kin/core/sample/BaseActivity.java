package kin.core.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import kin.core.KinClient;
import kin.sdk.core.sample.R;

public abstract class BaseActivity extends AppCompatActivity {

    final static int NO_ACTION_BAR_TITLE = -1;

    abstract Intent getBackIntent();

    abstract int getActionBarTitleRes();

    public boolean isMainNet() {
        if (getKinClient() != null && getKinClient().getEnvironment() != null) {
            return getKinClient().getEnvironment().isMainNet();
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

    protected void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
