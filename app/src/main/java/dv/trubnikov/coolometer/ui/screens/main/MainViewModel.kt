package dv.trubnikov.coolometer.ui.screens.main

import android.app.PendingIntent
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import dv.trubnikov.coolometer.domain.models.FakeMessage
import dv.trubnikov.coolometer.domain.models.FirebaseMessage
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.MessageParser
import dv.trubnikov.coolometer.domain.parsers.MessageParser.Companion.parse
import dv.trubnikov.coolometer.domain.parsers.MessageParser.Companion.serialize
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.domain.resositories.PreferenceRepository
import dv.trubnikov.coolometer.domain.workers.RetrieveTokenWorker
import dv.trubnikov.coolometer.domain.workers.UpdateWidgetWorker
import dv.trubnikov.coolometer.tools.*
import dv.trubnikov.coolometer.ui.notifications.Channel
import dv.trubnikov.coolometer.ui.views.ProgressMeterDrawer.Companion.MAX_PROGRESS
import dv.trubnikov.coolometer.ui.widget.ProgressMeterWidget
import dv.trubnikov.coolometer.ui.widget.WidgetUpdater
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val messageRepository: MessageRepository,
    private val tokenProvider: CloudTokenProvider,
    private val widgetUpdater: WidgetUpdater,
    private val workManager: WorkManager,
    private val parser: MessageParser,
) : ViewModel() {

    private val debugErrorHandler = CoroutineExceptionHandler { _, error ->
        Timber.e(error, "Ошибка при работе с дебаг панелью")
        stateFlow.value = State.Error
    }

    val stateFlow = MutableStateFlow<State>(State.Loading)
    val actionFlow = OneshotValueFlow<Action>()

    init {
        val errorHandler = CoroutineExceptionHandler { _, error ->
            Timber.e(error, "Не удалось вытащить неполученные достижения")
            stateFlow.value = State.Error
        }
        viewModelScope.launch(errorHandler) {
            messageRepository.getUnreceivedMessages().collect { messages ->
                stateFlow.value = createSuccessState(messages)
            }
        }
        viewModelScope.launch(errorHandler) {
            messageRepository.observeTotalScore().collect { totalProgress ->
                updateSuccessState { copy(totalProgress = totalProgress) }
                updateWidgets()
            }
        }
        scheduleWidgetUpdater()
        initializeCloudToken()
        onEntranceToTheApp()
    }

    fun markAsReceived(context: Context, message: Message) {
        val errorHandler = CoroutineExceptionHandler { _, error ->
            Timber.e(error, "Не удалось пометить достижение полученным")
            stateFlow.value = State.Error
        }
        viewModelScope.launch(errorHandler) {
            messageRepository.markAsReceived(message.messageId)
            offerToAddWidgetToHomeScreen(context)
        }
    }

    fun onFabClick() {
        val state = stateFlow.value as? State.Success ?: return
        val messages = state.unreceivedMessages
        val action = when {
            messages.size == 1 -> Action.AcceptDialog(messages.first())
            messages.size > 1 -> Action.ListDialog(messages)
            else -> {
                Timber.e("onFabClick call when there is no messages")
                return
            }
        }
        viewModelScope.launch {
            actionFlow.emit(action)
        }
    }

    fun onMessageFromNotification(intent: Intent?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (intent?.extras != null) {
                val message = parser.parse(intent).getOr { failure ->
                    Timber.e("Не удалось распарсить входной интент")
                    failure.logError()
                    return@launch
                }
                onMessageFromNotification(message)
            } else {
                Timber.i("В интенте нет бандла intent=[$intent]")
            }
        }
    }

    fun onDebugPanelOpen() {
        viewModelScope.launch(Dispatchers.IO + debugErrorHandler) {
            if (preferenceRepository.isDebugPanelFirstOpen) {
                preferenceRepository.isDebugPanelFirstOpen = false
                val message = FirebaseMessage(
                    messageId = "first_time_open_debug_panel",
                    text = "За нахождение дебаг-панели в приложении",
                    score = +42,
                    timestamp = System.currentTimeMillis(),
                )
                messageRepository.insertMessage(message).getOrThrow()
            }
        }
    }

    fun debugCopyToken(context: Context) {
        viewModelScope.launch(debugErrorHandler) {
            val token = withTimeoutOrNull(TOKEN_AWAIT_TIMEOUT_MS) {
                tokenProvider.getToken()
            }
            if (token != null) {
                val label = context.getString(R.string.debug_panel_copy_token_label)
                val clip = ClipData.newPlainText(label, token)
                context.getClipboardManager().setPrimaryClip(clip)
            } else {
                Toast.makeText(context, R.string.debug_panel_copy_token_fail, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun debugSetBigTicks(ticks: Int) {
        viewModelScope.launch(debugErrorHandler) {
            preferenceRepository.bigTicks = ticks
            updateSuccessState { copy(bigTicks = ticks) }
            updateWidgets()
        }
    }

    fun debugSetSmallTicks(ticks: Int) {
        viewModelScope.launch(debugErrorHandler) {
            preferenceRepository.smallTicks = ticks
            updateSuccessState { copy(smallTicks = ticks) }
            updateWidgets()
        }
    }

    fun debugToggleCoolButtons(isEnabled: Boolean) {
        viewModelScope.launch(debugErrorHandler) {
            preferenceRepository.enableDebugButtons = isEnabled
            updateSuccessState { copy(debugButtonEnable = isEnabled) }
        }
    }

    fun debugSendFakeNotification(context: Context) {
        viewModelScope.launch(debugErrorHandler) {
            createFakeNotification(context)
        }
    }

    fun debugDropConfetti() {
        viewModelScope.launch(debugErrorHandler) {
            actionFlow.emit(Action.DebugConfetti)
        }
    }

    fun debugAddFakeMessage() {
        viewModelScope.launch(debugErrorHandler) {
            messageRepository.insertMessage(FakeMessage())
        }
    }

    fun debugDeleteReceivedMessages() {
        viewModelScope.launch(debugErrorHandler) {
            val receivedMessages = messageRepository.getReceivedMessages().first()
            launch {
                for (message in receivedMessages) {
                    messageRepository.deleteMessage(message.messageId)
                }
            }
        }
    }

    fun debugDeleteFakeMessages() {
        viewModelScope.launch(debugErrorHandler) {
            val receivedMessages = messageRepository.getReceivedMessages().first()
            val fakeMessages = receivedMessages.filter {
                it.messageId.startsWith(FakeMessage.FAKE_ID_PREFIX)
            }
            launch {
                for (fake in fakeMessages) {
                    messageRepository.deleteMessage(fake.messageId)
                }
            }
        }
    }

    private suspend fun onMessageFromNotification(message: Message) {
        messageRepository.insertMessage(message)
        val messages = messageRepository.getUnreceivedMessages().first()
        val isUnreceived = messages.find { it.messageId == message.messageId } != null
        if (isUnreceived) {
            actionFlow.emit(Action.NotificationDialog(message))
        } else {
            actionFlow.emit(Action.PityDialog)
        }
    }

    private suspend fun createSuccessState(messages: List<Message>): State.Success {
        val totalProgress = messageRepository.getTotalScore().getOrThrow()
        val progress = totalProgress % MAX_PROGRESS
        return State.Success(
            bigTicks = preferenceRepository.bigTicks,
            smallTicks = preferenceRepository.smallTicks,
            progress = progress,
            totalProgress = totalProgress,
            unreceivedMessages = messages,
            debugButtonEnable = preferenceRepository.enableDebugButtons,
        )
    }

    private suspend fun updateWidgets() {
        withContext(Dispatchers.IO) {
            val totalScore = messageRepository.getTotalScore().getOrThrow()
            val smallTicks = preferenceRepository.smallTicks
            val bigTicks = preferenceRepository.bigTicks
            widgetUpdater.updateAllWidgets(totalScore, smallTicks, bigTicks)
        }
    }

    private fun createFakeNotification(context: Context) {
        val intent = parser.serialize<Intent>(FakeMessage()).getOr { return }
        intent.setClass(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(context, Channel.DEBUG.id)
            .setSmallIcon(R.drawable.ic_progress)
            .setContentTitle("Fake notification")
            .setContentText("Notification from the debug panel")
            .setContentIntent(pendingIntent)
            .setColor(context.getColor(R.color.secondaryDarkColor))
            .setAutoCancel(true)
            .build()

        Channel.DEBUG.init(context)
        val manager = context.getNotificationManager()
        manager.notify(0, notification)
    }

    private fun offerToAddWidgetToHomeScreen(context: Context) {
        if (!preferenceRepository.isWidgetOffered) {
            Timber.i("Offer to add widget to home screen")
            viewModelScope.launch {
                delay(WIDGET_OFFER_DELAY_MS)
                preferenceRepository.isWidgetOffered = true
                val widgetManager = context.getAppWidgetManager()
                val widgetComponent = ComponentName(context, ProgressMeterWidget::class.java)
                widgetManager.requestPinAppWidget(widgetComponent, null, null)
            }
        }
    }

    private fun onEntranceToTheApp() {
        if (preferenceRepository.isFirstEntrance) {
            val errorHandler = CoroutineExceptionHandler { _, error ->
                Timber.e(error, "Не удалось добавить приветственное сообщение")
                preferenceRepository.isFirstEntrance = true
                stateFlow.value = State.Error
            }
            viewModelScope.launch(errorHandler) {
                preferenceRepository.isFirstEntrance = false
                val entranceMessage = FirebaseMessage(
                    messageId = "first_entrance_id",
                    text = "Приветственные 150 баллов крутости за былые заслуги",
                    score = 150,
                    timestamp = System.currentTimeMillis()
                )
                messageRepository.insertMessage(entranceMessage).getOrThrow()
            }
        }
    }

    private fun scheduleWidgetUpdater() {
        viewModelScope.launch(Dispatchers.IO) {
            val widgetUpdater = PeriodicWorkRequestBuilder<UpdateWidgetWorker>(
                repeatInterval = Duration.ofHours(6)
            ).build()
            workManager.enqueueUniquePeriodicWork(
                WIDGET_UPDATER_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                widgetUpdater
            )
        }
    }

    private fun initializeCloudToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val retrieveToken = OneTimeWorkRequestBuilder<RetrieveTokenWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniqueWork(
                TOKEN_RETRIEVE_WORK,
                ExistingWorkPolicy.KEEP,
                retrieveToken,
            )
        }
    }

    private fun updateSuccessState(transform: State.Success.() -> State.Success) {
        val success = stateFlow.value as? State.Success
        if (success != null) {
            stateFlow.value = transform(success)
        }
    }

    sealed interface Action {
        class NotificationDialog(val message: Message) : Action
        class AcceptDialog(val message: Message) : Action
        class ListDialog(val messages: List<Message>) : Action
        object PityDialog : Action
        object DebugConfetti : Action
    }

    sealed interface State {
        object Loading : State

        object Error : State

        data class Success(
            val bigTicks: Int,
            val smallTicks: Int,
            val progress: Int,
            val totalProgress: Int,
            val unreceivedMessages: List<Message>,
            val debugButtonEnable: Boolean,
        ) : State
    }

    companion object {
        private const val WIDGET_UPDATER_WORK = "UpdateWidgetWorker_Periodic"
        private const val TOKEN_RETRIEVE_WORK = "RetrieveTokenWorker_OneShot"
        private const val WIDGET_OFFER_DELAY_MS = 1_000L
        private const val TOKEN_AWAIT_TIMEOUT_MS = 1_000L
    }
}
