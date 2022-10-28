package dv.trubnikov.coolometer.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dv.trubnikov.coolometer.app.logging.CoolometerTree
import timber.log.Timber.Forest.plant
import javax.inject.Inject

@HiltAndroidApp
class CoolometerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var loggingTree: CoolometerTree

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
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
}
