package kin.sdk

import android.os.Bundle
import android.support.multidex.MultiDex
import android.support.test.runner.AndroidJUnitRunner


class MultiDexAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle) {
        MultiDex.install(targetContext)

        super.onCreate(arguments)
    }
}
