package dv.trubnikov.coolometer.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
) : ViewModel() {

    val stateFlow = MutableStateFlow<State>(State.Loading)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            messageRepository.getReceivedMessages().map { messages ->
                stateFlow.value = createState(messages)
            }.catch {
                Timber.e("Fail to get received messages")
                stateFlow.value = State.Error
            }.collect()
        }
    }

    private fun createState(messages: List<Message>): State {
        if (messages.isEmpty()) {
            return State.Empty
        }
        val items = messages.mapIndexed { index, message ->
            HistoryItem(
                score = message.score,
                text = message.text,
                date = Date(message.timestamp),
                color = if (index % 2 == 0) {
                    android.R.color.white
                } else {
                    R.color.primaryLightColor
                }
            )
        }
        return State.Success(items)
    }

    sealed interface State {
        data class Success(
            val items: List<HistoryItem>
        ) : State

        object Loading : State
        object Empty : State
        object Error : State
    }
}
