package kin.sdk.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 */

public class WebWrapperActivity extends BaseActivity {

    public static final String TAG = WebWrapperActivity.class.getSimpleName();
    private static String ARGS_URL = "url";

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, WebWrapperActivity.class);
        intent.putExtra(ARGS_URL, url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_holder_activity);
        initWidgets();
    }

    private void initWidgets() {
        WebView webView = (WebView) findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        final String url = getIntent().getStringExtra(ARGS_URL);
        webView.loadUrl(url);
    }

    @Override
    Intent getBackIntent() {
        return ChooseNetworkActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.kin_foundation;
    }
}
