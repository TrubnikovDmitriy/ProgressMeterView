package dv.trubnikov.coolometer.domain.cloud

import dv.trubnikov.coolometer.domain.models.CloudMessage
import dv.trubnikov.coolometer.tools.OneshotValueFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudMessageQueue @Inject constructor() {

    private val innerMessageFlow = OneshotValueFlow<CloudMessage>()

    val messageFlow: SharedFlow<CloudMessage> = innerMessageFlow

    fun postNewMessage(message: CloudMessage) {
        innerMessageFlow.tryEmit(message)
    }
}