package kin.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.isA;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import kin.core.exception.CreateAccountException;
import kin.core.exception.DeleteAccountException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.stellar.sdk.KeyPair;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class KeyStoreImplTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private KeyStoreImpl keyStore;

    private static class ExceptionThrowerStore implements Store {

        HashMap<String, String> map = new HashMap<>();

        @Override
        public void saveString(@NonNull String key, @NonNull String value) {
            map.put(key, value);
        }

        @Nullable
        @Override
        public String getString(@NonNull String key) {
            return "NotJsonString";
        }

        @Override
        public void clear(@NonNull String key) {
            map.remove(key);
        }
    }

    @Before
    public void setup() {
        keyStore = new KeyStoreImpl(new FakeStore());
    }

    @Test
    public void newAccount() throws Exception {
        Account account = keyStore.newAccount();
        KeyPair keyPair = keyStore.decryptAccount(account);
        assertEquals(account.getAccountId(), keyPair.getAccountId());
        assertEquals(account.getEncryptedSeed(), String.valueOf(keyPair.getSecretSeed()));
    }

    @Test
    public void newAccount_JsonException_CreateAccountException() throws Exception {
        keyStore = new KeyStoreImpl(new ExceptionThrowerStore());

        expectedEx.expect(CreateAccountException.class);
        expectedEx.expectCause(isA(JSONException.class));
        keyStore.newAccount();
    }

    @Test
    public void loadAccounts() throws Exception {
        Account account1 = keyStore.newAccount();
        Account account2 = keyStore.newAccount();
        List<Account> accounts = keyStore.loadAccounts();
        Account actualAccount1 = accounts.get(0);
        Account actualAccount2 = accounts.get(1);
        assertEquals(account1.getEncryptedSeed(), actualAccount1.getEncryptedSeed());
        assertEquals(account1.getAccountId(), actualAccount1.getAccountId());
        assertEquals(account2.getEncryptedSeed(), actualAccount2.getEncryptedSeed());
        assertEquals(account2.getAccountId(), actualAccount2.getAccountId());
    }

    @Test
    public void loadAccounts_JsonException_LoadAccountException() throws Exception {
        Store stubStore = spy(FakeStore.class);
        when(stubStore.getString(anyString()))
            .thenReturn("not a real json");

        keyStore = new KeyStoreImpl(stubStore);

        expectedEx.expect(LoadAccountException.class);
        expectedEx.expectCause(isA(JSONException.class));
        keyStore.loadAccounts();
    }

    @Test
    public void deleteAccount() throws Exception {
        Account account1 = keyStore.newAccount();
        keyStore.newAccount();
        keyStore.deleteAccount(1);

        List<Account> accounts = keyStore.loadAccounts();
        assertEquals(1, accounts.size());
        assertEquals(account1.getAccountId(), accounts.get(0).getAccountId());
        assertEquals(account1.getEncryptedSeed(), accounts.get(0).getEncryptedSeed());
    }

    @Test
    public void deleteAccount_JsonException_DeleteAccountException() throws Exception {
        Store stubStore = spy(FakeStore.class);
        when(stubStore.getString(anyString()))
            .thenCallRealMethod()
            .thenReturn("not a real json");
        keyStore = new KeyStoreImpl(stubStore);

        keyStore.newAccount();
        expectedEx.expect(DeleteAccountException.class);
        expectedEx.expectCause(isA(JSONException.class));
        keyStore.deleteAccount(0);
    }

    @Test
    public void clearAllAccounts() throws Exception {
        keyStore.newAccount();
        keyStore.newAccount();
        keyStore.clearAllAccounts();
        assertTrue(keyStore.loadAccounts().isEmpty());
    }

}