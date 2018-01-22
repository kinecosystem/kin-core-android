package kin.sdk.core.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import kin.sdk.core.KinClient;

/**
 * User is given a choice to create or use an account on the MAIN or TEST(test) networks
 */
public class ChooseNetworkActivity extends BaseActivity {

    public static final String TAG = ChooseNetworkActivity.class.getSimpleName();
    private static final String KIN_FOUNDATION_URL = "https://github.com/kinfoundation";

    public static Intent getIntent(Context context) {
        return new Intent(context, ChooseNetworkActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_network_activity);
        initWidgets();
    }

    @Override
    protected boolean hasBack() {
        return false;
    }

    private void initWidgets() {
        TextView urlTextView = (TextView) findViewById(R.id.kin_foundation_url);
        urlTextView.setPaintFlags(urlTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        urlTextView.setText(Html.fromHtml(KIN_FOUNDATION_URL));
        urlTextView.setOnClickListener(view -> startWebWrapperActivity());
        findViewById(R.id.kin_icon).setOnClickListener(view -> startWebWrapperActivity());
        findViewById(R.id.btn_main_net).setOnClickListener(
            view -> createKinClient(KinClientSampleApplication.NetWorkType.MAIN));

        findViewById(R.id.btn_test_net).setOnClickListener(
            view -> createKinClient(KinClientSampleApplication.NetWorkType.TEST));
    }

    private void createKinClient(KinClientSampleApplication.NetWorkType netWorkType) {
        KinClientSampleApplication application = (KinClientSampleApplication) getApplication();
        KinClient kinClient = application.createKinClient(netWorkType);
        if (kinClient.hasAccount()) {
            startActivity(WalletActivity.getIntent(this));
        } else {
            startActivity(CreateWalletActivity.getIntent(this));
        }
    }

    private void startWebWrapperActivity(){
        startActivity(WebWrapperActivity.getIntent(this, KIN_FOUNDATION_URL));
    }

    @Override
    Intent getBackIntent() {
        return null;
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.app_name;
    }
}
