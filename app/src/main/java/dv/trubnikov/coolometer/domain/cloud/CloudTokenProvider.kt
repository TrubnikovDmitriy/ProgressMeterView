package dv.trubnikov.coolometer.domain.cloud

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
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
    private val firebaseAnalytics: FirebaseAnalytics,
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

    suspend fun getToken(): String {
        return tokenValue.first()
    }

    fun updateToken(token: String) {
        Timber.i("Update token")
        tokenValue.tryEmit(token)
    }

    private fun sendTokenToAnalytics(token: String) {
        Timber.i("Set token = $token")
        val splitToken = token.chunked(100) // max allowed size of params in FB Analytics
        firebaseAnalytics.logEvent("FCM") {
            splitToken.forEachIndexed { index, partOfToken ->
                param("token_part_$index", partOfToken)
            }
        }
    }
}
