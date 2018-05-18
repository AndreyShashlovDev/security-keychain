package com.sprinter.keychain.context

import android.app.Application
import android.os.StrictMode
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.sprinter.keychain.BuildConfig
import com.sprinter.keychain.R
import com.sprinter.keychain.utils.RootHelper
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import uk.co.chrisjenx.calligraphy.CalligraphyConfig


class AppDelegate : Application() {

    var appContext: AppContext? = null

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG && RootHelper.isDeviceRooted) {
            System.exit(0)
        }

        Fabric.with(
                this,
                Crashlytics.Builder()
                        .core(
                                CrashlyticsCore.Builder()
                                .disabled(BuildConfig.DEBUG)
                                .build()
                        ).build()
        )

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            StrictMode.enableDefaults()
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        appContext = AppContextFactory.createDefault(this.applicationContext)

        initCalligraphy()
    }

    private fun initCalligraphy() {
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder().setDefaultFontPath(
                "fonts/Roboto-Regular.ttf").setFontAttrId(R.attr.fontPath).build())
    }

}
