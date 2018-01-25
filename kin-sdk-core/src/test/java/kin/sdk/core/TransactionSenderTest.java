package kin.sdk.core;

import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.stellar.sdk.Server;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class TransactionSenderTest {

    private static final String ACCOUNT_ID_KIN_ISSUER = "GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM";
    private static final String ACCOUNT_ID = "GBQUCJ755LJBUFFKFZCTV7XFA6JUR5NAAEJF66SPCN3XROHVKSG3VVUY";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Server server;
    private MockWebServer mockWebServer;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("").toString();

        server = new Server(url);
    }

    @Test
    public void sendTransaction() throws Exception {

    }

}