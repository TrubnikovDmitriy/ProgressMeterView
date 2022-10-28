package dv.trubnikov.coolometer.domain.resositories

import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.Out
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    suspend fun insertMessage(message: Message): Out<Unit>

    suspend fun getReceivedMessages(): Flow<List<Message>>

    suspend fun getUnreceivedMessages(): Flow<List<Message>>

    suspend fun markAsReceived(messageId: String): Out<Unit>

    suspend fun deleteMessage(messageId: String): Out<Unit>

    suspend fun getTotalScore(): Out<Int>

    fun observeTotalScore(): Flow<Int>
}