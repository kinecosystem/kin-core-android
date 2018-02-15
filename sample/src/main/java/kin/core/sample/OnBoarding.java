package kin.core.sample;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import kin.core.KinAccount;
import kin.core.ResultCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

class OnBoarding {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String URL_CREATE_ACCOUNT = "http://188.166.34.7:8000/create_account";
    private static final String URL_FUND = "http://188.166.34.7:8000/fund";
    private final OkHttpClient okHttpClient;
    private final Handler handler;

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

    void onBoard(@NonNull KinAccount account, @NonNull String passphrase, @NonNull Callbacks callbacks) {

        Request request = new Request.Builder()
            .url(URL_CREATE_ACCOUNT)
            .post(createRequestBody(account))
            .build();
        okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    fireOnFailure(callbacks, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == 200) {
                        activateAccount(account, passphrase, callbacks);
                    } else {
                        fireOnFailure(callbacks, new Exception("Create account - response code is " + response.code()));
                    }
                }
            });

    }

    @NonNull
    private RequestBody createRequestBody(@NonNull KinAccount account) {
        return RequestBody.create(MEDIA_TYPE_JSON, createPublicAddressJsonString(account.getPublicAddress()));
    }

    @NonNull
    private String createPublicAddressJsonString(@NonNull String publicAddress) {
        JSONObject json = new JSONObject();
        try {
            json.put("public_address", publicAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void activateAccount(@NonNull KinAccount account, @NonNull String passphrase,
        @NonNull Callbacks callbacks) {
        account.activate(passphrase)
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
            .url(URL_FUND)
            .post(createRequestBody(account))
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
                    if (response.code() == 200) {
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
