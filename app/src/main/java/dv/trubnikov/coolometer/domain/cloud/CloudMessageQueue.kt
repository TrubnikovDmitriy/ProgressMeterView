package dv.trubnikov.coolometer.domain.cloud

import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.OneshotValueFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudMessageQueue @Inject constructor() {

    private val innerMessageFlow = OneshotValueFlow<Message>()

    val messageFlow: SharedFlow<Message> = innerMessageFlow

    fun postNewMessage(message: Message) {
        innerMessageFlow.tryEmit(message)
    }
}