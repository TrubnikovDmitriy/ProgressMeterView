package dv.trubnikov.coolometer.app

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import dv.trubnikov.coolometer.app.logging.CoolometerTree
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import timber.log.Timber
import timber.log.Timber.Forest.plant
import javax.inject.Inject

@HiltAndroidApp
class CoolometerApp : Application() {

    @Inject
    lateinit var cloudTokenProvider: CloudTokenProvider

    @Inject
    lateinit var loggingTree: CoolometerTree

    override fun onCreate() {
        super.onCreate()
        initTokenProvider()
        initTimber()
    }

    private fun initTimber() {
        plant(loggingTree)
    }

    private fun initTokenProvider() {
        cloudTokenProvider.init()
    }
}