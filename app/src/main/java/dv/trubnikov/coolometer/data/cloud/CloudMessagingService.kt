package dv.trubnikov.coolometer.data.cloud

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import dv.trubnikov.coolometer.domain.models.CloudMessageParser
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.assertFail
import dv.trubnikov.coolometer.tools.getOr
import dv.trubnikov.coolometer.tools.onFailure
import dv.trubnikov.coolometer.ui.widget.WidgetUpdater
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CloudMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(CoroutineName("CloudMessagingService") + Dispatchers.IO)

    @Inject
    lateinit var tokenAnalytics: CloudTokenProvider

    @Inject
    lateinit var repository: MessageRepository

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        handleMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNewToken(token: String) {
        tokenAnalytics.updateToken(token)
    }

    private fun handleMessage(message: RemoteMessage) {
        val cloudMessage = CloudMessageParser.parse(message)
        if (cloudMessage == null) {
            val error = IllegalArgumentException("Не удалось распарсить сообщение! [$message]")
            assertFail(error)
            return
        }
        repository.insertMessageBlocking(cloudMessage).onFailure {
            Timber.e("Не удалось записать сообщение в БД [$cloudMessage]")
        }
    }
}
