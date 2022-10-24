package dv.trubnikov.coolometer.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dv.trubnikov.coolometer.app.logging.CoolometerTree
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import timber.log.Timber.Forest.plant
import javax.inject.Inject

@HiltAndroidApp
class CoolometerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var cloudTokenProvider: CloudTokenProvider

    @Inject
    lateinit var loggingTree: CoolometerTree

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        initTokenProvider()
        initTimber()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun initTimber() {
        plant(loggingTree)
    }

    private fun initTokenProvider() {
        cloudTokenProvider.init()
    }
}