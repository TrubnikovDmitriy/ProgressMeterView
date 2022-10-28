package dv.trubnikov.coolometer.domain.cloud

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dv.trubnikov.coolometer.tools.ReplayValueFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudTokenProvider @Inject constructor(
    private val firebaseCrashlytics: FirebaseCrashlytics,
) {
    private val tokenScope = CoroutineScope(Dispatchers.IO)
    private val tokenValue = ReplayValueFlow<String>()

    init {
        tokenScope.launch {
            tokenValue.collect { token ->
                sendTokenToAnalytics(token)
            }
        }
    }

    suspend fun getToken(): String {
        return tokenValue.first()
    }

    fun updateToken(token: String) {
        Timber.i("Update token")
        tokenValue.tryEmit(token)
    }

    private fun sendTokenToAnalytics(token: String) {
        Timber.i("Set token (${token.length}) = [$token]")
        firebaseCrashlytics.setUserId(token)
        firebaseCrashlytics.recordException(RuntimeException("Fake exception"))
    }
}
