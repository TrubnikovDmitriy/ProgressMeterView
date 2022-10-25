package dv.trubnikov.coolometer.domain.cloud

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dv.trubnikov.coolometer.tools.ReplayValueFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudTokenProvider @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) {
    private val tokenScope = CoroutineScope(Dispatchers.IO)
    private val tokenValue = ReplayValueFlow<String>()

    init {
        tokenScope.launch {
            tokenValue.emit(firebaseMessaging.token.await())
            tokenValue.collect { token ->
                sendTokenToAnalytics(token)
            }
        }
    }

    // It is necessary since instance is lazy
    fun init() = Unit

    suspend fun getToken(): String {
        return tokenValue.first()
    }

    fun updateToken(token: String) {
        Timber.i("Update token")
        tokenValue.tryEmit(token)
    }

    @SuppressLint("LogNotTimber") // Timber is not initialized yet
    private fun sendTokenToAnalytics(token: String) {
        Log.i("CloudTokenProvider", "Set token (${token.length}) = [$token]")
        firebaseCrashlytics.setUserId(token)
        firebaseCrashlytics.recordException(RuntimeException("Fake exception"))
    }
}
