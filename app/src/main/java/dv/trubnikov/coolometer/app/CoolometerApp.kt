package dv.trubnikov.coolometer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dv.trubnikov.coolometer.BuildConfig
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import timber.log.Timber
import timber.log.Timber.Forest.plant
import javax.inject.Inject

@HiltAndroidApp
class CoolometerApp : Application() {

    @Inject
    lateinit var cloudTokenProvider: CloudTokenProvider

    override fun onCreate() {
        super.onCreate()
        initTokenProvider()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
    }

    private fun initTokenProvider() {
        cloudTokenProvider.init()
    }
}