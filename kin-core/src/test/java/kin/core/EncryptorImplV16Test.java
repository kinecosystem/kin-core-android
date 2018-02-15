package kin.core;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class EncryptorImplV16Test {

    private Encryptor cryptor;

    @Before
    public void setup() {
        cryptor = new EncryptorImplV16();
    }

    @Test
    public void encryptThenDecrypt_Success() throws Exception {
        String secretSeed = "SDL34EFTWFPIVJLUYSCVZJFYPYLIGFHDYG5VKOVBQYLU6H4OWRPETCAE";
        String encryptedSecret = cryptor.encrypt(secretSeed);
        String actualDecryptedSeed = cryptor.decrypt(encryptedSecret);
        assertEquals(secretSeed, actualDecryptedSeed);
    }

    @Test
    public void decrypt_NotEqualsToSource() throws Exception {
        String secret = "SCJFLXKUY6VQT2LYSP6XDP23WNEP5OITSC3LZEJUJO7GFZM7QLDF2BCN";
        String encryptedSecret = cryptor.encrypt(secret);
        //V16 encryptor is no-op that do not encrypt
        assertEquals(encryptedSecret, secret);
    }

}