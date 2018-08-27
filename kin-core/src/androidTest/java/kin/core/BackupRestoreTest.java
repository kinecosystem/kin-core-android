package kin.core;


import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import kin.core.exception.CryptoException;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.stellar.sdk.KeyPair;

public class BackupRestoreTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private BackupRestoreImpl backupRestore = new BackupRestoreImpl();

    @Test
    public void backupAndRestore_Success() throws CryptoException {
        KeyPair keyPair = KeyPair.random();
        String exportedJson = backupRestore.exportWallet(keyPair, "1234567890abcefghijkl");
        KeyPair importKeyPair = backupRestore.importWallet(exportedJson, "1234567890abcefghijkl");
        assertThat(importKeyPair.getSecretSeed(), equalTo(keyPair.getSecretSeed()));
    }

    @Test
    public void backupAndRestore_WrongPassphrase_CryptoException() throws CryptoException {
        expectedEx.expect(CryptoException.class);

        KeyPair keyPair = KeyPair.random();
        String exportedJson = backupRestore.exportWallet(keyPair, "1234567890abcefghijkl");
        backupRestore.importWallet(exportedJson, "1234567890abcefghijklX");
    }

    @Test
    public void import_BadJson_CryptoException() throws CryptoException {
        expectedEx.expect(CryptoException.class);
        expectedEx.expectCause(isA(JSONException.class));

        backupRestore.importWallet("not a real json!!", "123456");
    }

    @Test
    public void importFromIOS() throws CryptoException {
        testImportBackup("{\n"
                + "  \"pkey\" : \"GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S\",\n"
                + "  \"seed\" : \"f5b162bf9bfa93922b709b00a89e5bf7f61eef38717b35dabbabc73a68be77e2b498c5697f99c3f70882a8a11cc5e34f88b6f069f47443dbfa031fadd12e8b6af1cc142c902cfef9\",\n"
                + "  \"salt\" : \"d00564d4887b4ccade9f2b63211c37c4\"\n"
                + "}",
            "123456",
            "GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S");
        testImportBackup("{\n"
                + "  \"pkey\" : \"GDNLGOSLTZMZX5GNZ4FY26DPXLGXEIFJJ7VWDJVN3L5DOG7TFLZXHNCU\",\n"
                + "  \"seed\" : \"0d2a1bab487f5591277692987bd66c6f07199120f3e8fab6d013ac81bc3bd648fa357d90b39178d105da130705ee6e8beaea4a5cf900b53978d8c8fecd72fee7bd5aa7295a619b9d\",\n"
                + "  \"salt\" : \"7fb38499e44f084958e954b73f1c2cf0\"\n"
                + "}",
            "123456",
            "GDNLGOSLTZMZX5GNZ4FY26DPXLGXEIFJJ7VWDJVN3L5DOG7TFLZXHNCU");
    }

    private void testImportBackup(String exportedJson, String passphrase, String publicKey) throws CryptoException {
        KeyPair importKeyPair = backupRestore.importWallet(exportedJson, passphrase);
        assertThat(importKeyPair.getAccountId(), equalTo(publicKey));
    }


}
