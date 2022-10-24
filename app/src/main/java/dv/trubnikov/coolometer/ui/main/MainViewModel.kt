package dv.trubnikov.coolometer.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dv.trubnikov.coolometer.domain.models.CloudMessageParser
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.OneshotValueFlow
import dv.trubnikov.coolometer.tools.assertFail
import dv.trubnikov.coolometer.tools.getOrThrow
import dv.trubnikov.coolometer.ui.views.ProgressMeterDrawer.Companion.MAX_PROGRESS
import dv.trubnikov.coolometer.ui.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

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
    }

    fun markAsReceived(message: Message) {
        val errorHandler = CoroutineExceptionHandler { _, error ->
            Timber.e(error, "Не удалось пометить достижение полученным")
            stateFlow.value = State.Error
        }
        viewModelScope.launch(errorHandler) {
            messageRepository.markAsReceived(message.messageId)
            val totalScore = messageRepository.getTotalScore().getOrThrow()
            widgetUpdater.updateAllWidgets(totalScore)
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
            if (intent?.hasExtra(CMS_MARKER_KEY) == true) {
                intent.removeExtra(CMS_MARKER_KEY)
                val message = CloudMessageParser.parse(intent)
                if (message != null) {
                    onMessageFromNotification(message)
                } else {
                    val error = IllegalStateException(
                        """
                    Не удалось распарсить интент intent=[$intent], 
                    extras=[${intent.extras?.toString()}]
                    """.trimIndent()
                    )
                    assertFail(error)
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
        val bigTicks = 5
        val smallTicks = 2
        val totalProgress = messageRepository.getTotalScore().getOrThrow()
        val progress = totalProgress % MAX_PROGRESS
        return State.Success(
            bigTicks = bigTicks,
            smallTicks = smallTicks,
            progress = progress,
            totalProgress = totalProgress,
            unreceivedMessages = messages,
        )
    }

    sealed interface Action {
        class NotificationDialog(val message: Message) : Action
        class AcceptDialog(val message: Message) : Action
        class ListDialog(val messages: List<Message>) : Action
        object PityDialog : Action
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
        ) : State
    }

    companion object {
        private const val CMS_MARKER_KEY = "google.ttl"
    }
}
