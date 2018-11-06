package kin.sdk;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static kin.sdk.AccountStatus.ACTIVATED;
import static kin.sdk.AccountStatus.NOT_ACTIVATED;
import static kin.sdk.AccountStatus.NOT_CREATED;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;

@Retention(SOURCE)
@IntDef({NOT_CREATED, NOT_ACTIVATED, ACTIVATED})
public @interface AccountStatus {

    /**
     * Account was not created on blockchain network, account should be created and funded by a different account on
     * the blockchain.
     */
    int NOT_CREATED = 0;
    /**
     * Account was created, but not activated yet, thus cannot send or receive kin yet. call {@link
     * KinAccount#activate()} for activating.
     */
    int NOT_ACTIVATED = 1;
    /**
     * Account was created and activated, account is ready to use with kin.
     */
    int ACTIVATED = 2;
}
