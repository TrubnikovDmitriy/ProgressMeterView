package dv.trubnikov.coolometer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dv.trubnikov.coolometer.BuildConfig
import timber.log.Timber
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class CoolometerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
    }
}