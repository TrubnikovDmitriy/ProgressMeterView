package dv.trubnikov.coolometer.cloud

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.models.CloudMessageParser
import dv.trubnikov.coolometer.tools.assertFail
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CloudMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenAnalytics: CloudTokenProvider

    @Inject
    lateinit var messageQueue: CloudMessageQueue

    override fun onCreate() {
        Timber.d("KekPek onCreate()")
        super.onCreate()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("KekPek onMessageReceived()")
        super.onMessageReceived(message)
        sendMessageToRunningActivity(message)
    }

    override fun onNewToken(token: String) {
        tokenAnalytics.updateToken(token)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("KekPek onDestroy()")
    }

    /**
     * We can sure about it, because [onMessageReceived] is called
     * only when application is in foreground (message must be with
     * both notification & data).
     */
    private fun sendMessageToRunningActivity(message: RemoteMessage) {
        if (message.notification == null) {
            val error = IllegalArgumentException("Пришло сообщение без нотификации! [$message]")
            assertFail(error)
            return
        }

        val cloudMessage = CloudMessageParser.parse(message)
        if (cloudMessage == null) {
            val error = IllegalArgumentException("Не удалось распарсить сообщение! [$message]")
            assertFail(error)
            return
        }

        messageQueue.postNewMessage(cloudMessage)
    }
}
