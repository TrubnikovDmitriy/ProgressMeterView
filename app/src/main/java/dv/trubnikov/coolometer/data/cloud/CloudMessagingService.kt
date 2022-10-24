package dv.trubnikov.coolometer.data.cloud

import androidx.annotation.WorkerThread
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import dv.trubnikov.coolometer.domain.models.CloudMessageParser
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
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

    @WorkerThread
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("onMessageReceived - ${message.data}")
        handleMessage(message)
    }

    override fun onDestroy() {
        Timber.i("Service is destroying")
        super.onDestroy()
        scope.cancel()
    }

    override fun onNewToken(token: String) {
        tokenAnalytics.updateToken(token)
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val message = CloudMessageParser.parse(remoteMessage)
        if (message != null) {
            scheduleJob(message)
            handleNow(message)
        } else {
            Timber.e("Не удалось распарсить сообщение! [$remoteMessage]")
        }
    }

    private fun scheduleJob(message: Message) {

    }

    @WorkerThread
    private fun handleNow(message: Message) {
        val job = scope.launch {
            repository.insertMessage(message).onFailure {
                Timber.e("Не удалось записать сообщение в БД [$message]")
            }
        }
        // We can do it, since it is a worker thread.
        // On the other, hand if we return to early,
        // this service will be destroyed.
        //
        // So we take as much time as it possible. If we are interrupted,
        // the job from [scheduleJob] will complete handling instead of us,
        // but a little bit later.
        while (!job.isCompleted) {
            try {
                Timber.d("Wait for coroutine completion")
                Thread.sleep(250)
            } catch (e: InterruptedException) {
                Timber.d(e, "Worker thread was interrupted")
                job.cancel(e.message ?: "Thread was interrupted", e)
                Thread.currentThread().interrupt()
                return
            }
        }
        Timber.d("Coroutine completed")
    }
}
