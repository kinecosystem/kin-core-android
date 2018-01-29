package kin.sdk.core;


import android.support.annotation.NonNull;
import java.io.InputStream;
import okhttp3.mockwebserver.MockResponse;

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
}
