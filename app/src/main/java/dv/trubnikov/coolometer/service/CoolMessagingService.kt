package dv.trubnikov.coolometer.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dv.trubnikov.coolometer.messaging.CloudTokenProvider
import javax.inject.Inject

class CoolMessagingService @Inject constructor(
    private val tokenAnalytics: CloudTokenProvider,
) : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        tokenAnalytics.updateToken(token)
    }
}
