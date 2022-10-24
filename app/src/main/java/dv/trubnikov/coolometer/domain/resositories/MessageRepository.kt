package dv.trubnikov.coolometer.domain.resositories

import androidx.annotation.WorkerThread
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.Out
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    @Deprecated("Сделать expedited job")
    @WorkerThread
    fun insertMessageBlocking(message: Message): Out<Unit>

    suspend fun insertMessage(message: Message): Out<Unit>

    suspend fun getUnreceivedMessages(): Flow<List<Message>>

    suspend fun markAsReceived(messageId: String): Out<Unit>

    suspend fun getTotalScore(): Out<Int>
}