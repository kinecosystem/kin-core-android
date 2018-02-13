package kin.core;


import android.support.annotation.NonNull;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

final class TestUtils {

    static String loadResource(Class clazz, String res) {
        InputStream is = clazz.getClassLoader()
            .getResourceAsStream(res);
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @NonNull
    static MockResponse generateSuccessMockResponse(Class clazz, String res) {
        return new MockResponse()
            .setBody(loadResource(clazz, res))
            .setResponseCode(200);
    }

    static void enqueueEmptyResponse(MockWebServer mockWebServer) {
        //simulate http 200 with no body, will cause to parse empty body and response will be null
        mockWebServer.enqueue(new MockResponse().setBodyDelay(1, TimeUnit.SECONDS));
    }
}
