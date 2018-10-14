package kin.core.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import kin.core.KinAccount;
import kin.core.PaymentInfo;
import kin.core.Request;
import kin.core.PaymentsHistoryRequestParams;
import kin.sdk.core.sample.R;

public class ShowPaymentsHistoryActivity extends BaseActivity {

    public static final String TAG = ShowPaymentsHistoryActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private View progressBar;
    private KinAccount account;

    public static Intent getIntent(Context context) {
        return new Intent(context, ShowPaymentsHistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_transaction_history);
        account = getKinClient().getAccount(0);

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recyclerview);

        PaymentsHistoryRequestParams params = getIntent().getParcelableExtra(PaymentsHistoryActivity.EXTRA_TRANSACTION_HISTORY_PARAMS);
        getTransactionHistory(params);
    }

    @Override
    Intent getBackIntent() {
        return PaymentsHistoryActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.transaction_history_list;
    }

    private void initRecyclerView(List<PaymentInfo> payments) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        PaymentsHistoryRecyclerViewAdapter adapter = new PaymentsHistoryRecyclerViewAdapter(payments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    private void getTransactionHistory(PaymentsHistoryRequestParams params) {
        if (account != null) {
            Request<List<PaymentInfo>> request = account.getPaymentsHistory(params);
            request.run(new DisplayCallback<List<PaymentInfo>>(progressBar) {
                @Override
                public void displayResult(Context context, View displayView, List<PaymentInfo> result) {
                    initRecyclerView(result);
                }
            });
        }
    }

}
