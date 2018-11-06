package kin.sdk;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.DeleteAccountException;
import org.json.JSONException;
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

    @Test
    public void newAccount() throws Exception {
        KeyStoreImpl keyStore = new KeyStoreImpl(new FakeStore(), new FakeBackupRestore());
        KeyPair account = keyStore.newAccount();
        assertNotNull(account);
        assertNotNull(account.getPublicKey());
        assertNotNull(account.getSecretSeed());
    }

    @Test
    public void newAccount_JsonException_CreateAccountException() throws Exception {
        Store mockStore = mock(Store.class);
        when(mockStore.getString(anyString()))
            .thenReturn(KeyStoreImpl.ENCRYPTION_VERSION_NAME)
            .thenReturn("not a real json");
        KeyStoreImpl keyStore = new KeyStoreImpl(mockStore, new FakeBackupRestore());

        expectedEx.expect(CreateAccountException.class);
        expectedEx.expectCause(isA(JSONException.class));
        keyStore.newAccount();
    }

    @Test
    public void loadAccounts_OldVersionData_DropOldData() throws Exception {
        FakeStore fakeStore = new FakeStore();
        fakeStore.saveString(KeyStoreImpl.VERSION_KEY, "some_version");
        fakeStore.saveString(KeyStoreImpl.STORE_KEY_ACCOUNTS,
            "{&quot;accounts&quot;:[{&quot;seed&quot;:&quot;{\\&quot;iv\\&quot;:\\&quot;nVGsoEHgjW4xw2gx\\\\n\\&quot;,\\&quot;cipher\\&quot;:\\&quot;kEC64vaQu\\\\\\/erpFvvnrY+sWm\\\\\\/o4GjmjPfgG31zQTwvp0taxo\\\\\\/04PoaisjfEQxrydRwBGFvG\\\\\\/nG345\\\\ntXMn+x2H0jnaPWWCznPA\\\\n\\&quot;}&quot;,&quot;public_key&quot;:&quot;GBYPGYWPWHWSVTQGTUCH2IICIP2PRLN3QYSUX5NOHHMNDQW26A4WK2IK&quot;}]}");
        KeyStoreImpl keyStore = new KeyStoreImpl(fakeStore, new FakeBackupRestore());

        keyStore.loadAccounts();
        assertThat(fakeStore.getString(KeyStoreImpl.VERSION_KEY), equalTo(KeyStoreImpl.ENCRYPTION_VERSION_NAME));
        assertThat(fakeStore.getString(KeyStoreImpl.STORE_KEY_ACCOUNTS), nullValue());
    }

    @Test
    public void loadAccounts() throws Exception {
        KeyStoreImpl keyStore = new KeyStoreImpl(new FakeStore(), new FakeBackupRestore());
        KeyPair account1 = keyStore.newAccount();
        KeyPair account2 = keyStore.newAccount();
        List<KeyPair> accounts = keyStore.loadAccounts();
        KeyPair actualAccount1 = accounts.get(0);
        KeyPair actualAccount2 = accounts.get(1);
        assertEquals(String.valueOf(account1.getSecretSeed()), String.valueOf(actualAccount1.getSecretSeed()));
        assertEquals(String.valueOf(account2.getSecretSeed()), String.valueOf(actualAccount2.getSecretSeed()));
    }

    @Test
    public void loadAccounts_JsonException_LoadAccountException() throws Exception {
        Store mockStore = mock(Store.class);
        when(mockStore.getString(anyString()))
            .thenReturn(KeyStoreImpl.ENCRYPTION_VERSION_NAME)
            .thenReturn("not a real json");
        KeyStoreImpl keyStore = new KeyStoreImpl(mockStore, new FakeBackupRestore());

        expectedEx.expect(LoadAccountException.class);
        expectedEx.expectCause(isA(JSONException.class));
        keyStore.loadAccounts();
    }

    @Test
    public void deleteAccount() throws Exception {
        KeyStoreImpl keyStore = new KeyStoreImpl(new FakeStore(), new FakeBackupRestore());
        KeyPair account1 = keyStore.newAccount();
        keyStore.newAccount();
        keyStore.deleteAccount(1);

        List<KeyPair> accounts = keyStore.loadAccounts();
        assertEquals(1, accounts.size());
        assertEquals(String.valueOf(account1.getSecretSeed()), String.valueOf(accounts.get(0).getSecretSeed()));
    }

    @Test
    public void deleteAccount_JsonException_DeleteAccountException() throws Exception {
        Store stubStore = spy(FakeStore.class);
        when(stubStore.getString(anyString()))
            .thenCallRealMethod()
            .thenCallRealMethod()
            .thenReturn("not a real json");
        KeyStoreImpl keyStore = new KeyStoreImpl(stubStore, new FakeBackupRestore());

        keyStore.newAccount();
        expectedEx.expect(DeleteAccountException.class);
        expectedEx.expectCause(isA(JSONException.class));
        keyStore.deleteAccount(0);
    }

    @Test
    public void clearAllAccounts() throws Exception {
        KeyStoreImpl keyStore = new KeyStoreImpl(new FakeStore(), new FakeBackupRestore());
        keyStore.newAccount();
        keyStore.newAccount();
        keyStore.clearAllAccounts();
        assertTrue(keyStore.loadAccounts().isEmpty());
    }

}