package kin.sdk.core;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;

public class EncryptorTest {

    private Encryptor cryptor;

    @Before
    public void setup() {
        Context context = InstrumentationRegistry.getTargetContext();
        cryptor = EncryptorFactory
            .create(context, new SharedPrefStore(context.getSharedPreferences("TEST", Context.MODE_PRIVATE)));
    }

    @Test
    public void encryptThenDecrypt_Success() throws Exception {
        String secretSeed = "SDL34EFTWFPIVJLUYSCVZJFYPYLIGFHDYG5VKOVBQYLU6H4OWRPETCAE";
        String encryptedSecret = cryptor.encrypt(secretSeed);
        String actualDecryptedSeed = cryptor.decrypt(encryptedSecret);
        assertEquals(secretSeed, actualDecryptedSeed);
    }

    @Test
    public void encrypt_VerifyRandomness() throws Exception {
        String encryptedSecret1 = cryptor.encrypt("SCIZAKL7BH7XHL2Y3HZ6SOCMKMTTMHASD5LRHJJKZOHBQARTCFNWAPEF");
        String encryptedSecret2 = cryptor.encrypt("SCIZAKL7BH7XHL2Y3HZ6SOCMKMTTMHASD5LRHJJKZOHBQARTCFNWAPEF");
        assertNotEquals(encryptedSecret1, encryptedSecret2);
    }

    @Test
    public void decrypt_NotEqualsToSource() throws Exception {
        String secret = "SCJFLXKUY6VQT2LYSP6XDP23WNEP5OITSC3LZEJUJO7GFZM7QLDF2BCN";
        String encryptedSecret = cryptor.encrypt(secret);
        assertNotEquals(encryptedSecret, secret);
    }

}