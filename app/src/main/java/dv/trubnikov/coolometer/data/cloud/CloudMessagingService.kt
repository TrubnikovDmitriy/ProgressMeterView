package dv.trubnikov.coolometer.data.cloud

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.MessageParser
import dv.trubnikov.coolometer.domain.parsers.MessageParser.Companion.parse
import dv.trubnikov.coolometer.domain.parsers.MessageParser.Companion.serialize
import dv.trubnikov.coolometer.domain.workers.HandleMessageWorker
import dv.trubnikov.coolometer.tools.getOr
import dv.trubnikov.coolometer.tools.logError
import dv.trubnikov.coolometer.tools.onFailure
import dv.trubnikov.coolometer.tools.onSuccess
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CloudMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokenAnalytics: CloudTokenProvider
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var parser: MessageParser

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("onMessageReceived - ${message.data}")
        handleMessage(message)
    }

    override fun onNewToken(token: String) {
        tokenAnalytics.updateToken(token)
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        parser.parse(remoteMessage)
            .onSuccess { scheduleJob(it) }
            .onFailure { it.logError() }
    }

    private fun scheduleJob(message: Message) {
        val data = parser.serialize<Data>(message).getOr { return }
        val updateDb = OneTimeWorkRequestBuilder<HandleMessageWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(data)
            .build()
        workManager.enqueue(updateDb)
        Timber.i("The works are enqueued")
    }
}
