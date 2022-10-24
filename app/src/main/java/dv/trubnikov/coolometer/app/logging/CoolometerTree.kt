package dv.trubnikov.coolometer.app.logging

import android.util.Log
import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject

class CoolometerTree @Inject constructor(
    private val firebaseCrashlytics: FirebaseCrashlytics
) : Timber.DebugTree() {

    companion object {
        private const val APP_TAG = "KekPek"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            sendToCrashlytics(message, t)
        }
        super.log(priority, APP_TAG, message, t)
        if (priority == Log.ASSERT && BuildConfig.DEBUG) {
            crashOnWtf(message, t)
        }
    }

    private fun sendToCrashlytics(message: String, error: Throwable?) {
        if (error != null) {
            firebaseCrashlytics.log(message)
            firebaseCrashlytics.recordException(error)
        } else {
            val artificialError = Throwable("Artificial error: [$message]")
            firebaseCrashlytics.recordException(artificialError)
        }
    }

    private fun crashOnWtf(message: String, error: Throwable?) {
        if (error != null) {
            throw error
        } else {
            throw RuntimeException("Artificial error: [$message]")
        }
    }
}
