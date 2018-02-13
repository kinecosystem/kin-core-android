package kin.core;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.os.Build;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class EncryptorFactoryTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Version = {0}, Upgraded to = {1}, Expected Encryptor = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {16, 0, EncryptorImplV16.class},
            {17, 0, EncryptorImplV16.class},
            {18, 0, EncryptorImplV18.class},
            {19, 0, EncryptorImplV18.class},
            {20, 0, EncryptorImplV18.class},
            {21, 0, EncryptorImplV18.class},
            {22, 0, EncryptorImplV18.class},
            {23, 0, EncryptorImplV23.class},
            {24, 0, EncryptorImplV23.class},
            {25, 0, EncryptorImplV23.class},
            {26, 0, EncryptorImplV23.class},
            {27, 0, EncryptorImplV23.class},
            {28, 0, EncryptorImplV23.class},

            {16, 17, EncryptorImplV16.class},
            {17, 26, EncryptorImplV16.class},
            {16, 18, EncryptorImplV16.class},
            {16, 23, EncryptorImplV16.class},

            {18, 19, EncryptorImplV18.class},
            {18, 23, EncryptorImplV18.class},
            {19, 26, EncryptorImplV18.class},
            {20, 26, EncryptorImplV18.class},
            {22, 23, EncryptorImplV18.class},

            {23, 24, EncryptorImplV23.class},
            {24, 25, EncryptorImplV23.class},
            {25, 26, EncryptorImplV23.class},
        });
    }

    private int upgradedVersion;
    private Class<?> expectedEncryptorClass;

    public EncryptorFactoryTest(int androidVersion, int upgradedVersion, Class<?> expectedEncryptorClass) {
        setAndroidSDKVersion(androidVersion);
        this.expectedEncryptorClass = expectedEncryptorClass;
        this.upgradedVersion = upgradedVersion;
    }

    @Test
    public void create() throws Exception {
        Store store = new FakeStore();
        if (upgradedVersion != 0) {
            simulateUpgradeOfAndroidVersion(store);
        }
        Encryptor encryptor = EncryptorFactory.create(RuntimeEnvironment.application, store);

        assertThat(encryptor, is(instanceOf(expectedEncryptorClass)));
    }

    private void simulateUpgradeOfAndroidVersion(Store store) {
        //create should save the OS version Encryptor was created with
        EncryptorFactory.create(RuntimeEnvironment.application, store);
        //now change the SDK (simulate upgrade)
        setAndroidSDKVersion(upgradedVersion);
    }

    private void setAndroidSDKVersion(int version) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", version);
    }
}