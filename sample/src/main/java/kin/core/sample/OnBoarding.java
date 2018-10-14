package kin.core.sample;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import kin.core.KinAccount;
import kin.core.ListenerRegistration;
import kin.core.ResultCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class OnBoarding {

    private static final String URL_CREATE_ACCOUNT = "http://friendbot-playground.kininfrastructure.com/?addr=";
    private static final int FUND_KIN_AMOUNT = 6000;
    private static final String URL_FUND =
        "http://faucet-playground.kininfrastructure.com/fund?account=%s&amount=" + String.valueOf(FUND_KIN_AMOUNT);
    private final OkHttpClient okHttpClient;
    private final Handler handler;
    private ListenerRegistration listenerRegistration;

    public interface Callbacks {

        void onSuccess();

        void onFailure(Exception e);
    }

    OnBoarding() {
        handler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    }

    void onBoard(@NonNull KinAccount account, @NonNull Callbacks callbacks) {
        Runnable accountCreationListeningTimeout = () -> {
            listenerRegistration.remove();
            fireOnFailure(callbacks, new TimeoutException("Waiting for account creation event time out"));
        };

        listenerRegistration = account.addAccountCreationListener(data -> {
            listenerRegistration.remove();
            handler.removeCallbacks(accountCreationListeningTimeout);
            activateAccount(account, callbacks);
        });
        handler.postDelayed(accountCreationListeningTimeout, 10 * DateUtils.SECOND_IN_MILLIS);
        createAccount(account, callbacks);
    }

    private void createAccount(@NonNull KinAccount account, @NonNull Callbacks callbacks) {
        Request request = new Request.Builder()
            .url(URL_CREATE_ACCOUNT + account.getPublicAddress())
            .get()
            .build();
        okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    fireOnFailure(callbacks, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    int code = response.code();
                    response.close();
                    if (code != 200) {
                        fireOnFailure(callbacks, new Exception("Create account - response code is " + response.code()));
                    }
                }
            });

    }

    private void activateAccount(@NonNull KinAccount account, @NonNull Callbacks callbacks) {
        account.activate()
            .run(new ResultCallback<Void>() {
                @Override
                public void onResult(Void result) {
                    //This is not mandatory part of onboarding, account is now ready to send/receive kin
                    fundAccountWithKin(account, callbacks);
                }

                @Override
                public void onError(Exception e) {
                    fireOnFailure(callbacks, e);
                }
            });
    }

    private void fundAccountWithKin(KinAccount account, @NonNull Callbacks callbacks) {
        Request request = new Request.Builder()
            .url(String.format(URL_FUND, account.getPublicAddress()))
            .get()
            .build();
        okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    fireOnFailure(callbacks, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                    int code = response.code();
                    response.close();
                    if (code == 200) {
                        //will trigger a call to get updated balance
                        fireOnSuccess(callbacks);
                    } else {
                        fireOnFailure(callbacks, new Exception("Fund account - response code is " + response.code()));
                    }
                }
            });
    }

    private void fireOnFailure(@NonNull Callbacks callbacks, Exception ex) {
        handler.post(() -> callbacks.onFailure(ex));
    }

    private void fireOnSuccess(@NonNull Callbacks callbacks) {
        handler.post(callbacks::onSuccess);
    }
}
