package dv.trubnikov.coolometer.data.room.repositories

import android.database.sqlite.SQLiteException
import androidx.annotation.WorkerThread
import dv.trubnikov.coolometer.data.room.dao.MessageDao
import dv.trubnikov.coolometer.data.room.tables.MessageEntity
import dv.trubnikov.coolometer.domain.models.FirebaseMessage
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.Out
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMessageRepository @Inject constructor(
    private val messageDao: MessageDao,
) : MessageRepository {

    @WorkerThread
    override fun insertMessageBlocking(message: Message): Out<Unit> {
        return try {
            val entity = message.toEntity()
            messageDao.insertMessageBlocking(entity)
            Out.Success(Unit)
        } catch (e: SQLiteException) {
            Out.Failure(e)
        }
    }

    override suspend fun insertMessage(message: Message): Out<Unit> {
        return safeDatabaseRequest(Dispatchers.IO) {
            val entity = message.toEntity()
            messageDao.insertMessage(entity)
        }
    }

    override suspend fun getUnreceivedMessages(): Flow<List<Message>> {
        return flow {
            messageDao.getUnreceivedMessages().collect { messages ->
                val models = messages.map { it.toModel() }
                emit(models)
            }
        }
    }

    override suspend fun markAsReceived(messageId: String): Out<Unit> {
        return safeDatabaseRequest(Dispatchers.IO) {
            messageDao.markAsReceived(messageId)
        }
    }

    override suspend fun getTotalScore(): Out<Int> {
        return safeDatabaseRequest(Dispatchers.IO) {
            messageDao.getTotalScore()
        }
    }

    private suspend fun <R> safeDatabaseRequest(
        dispatchers: CoroutineDispatcher,
        request: suspend () -> R,
    ): Out<R> {
        return try {
            withContext(dispatchers) {
                val response = request()
                Out.Success(response)
            }
        } catch (e: SQLiteException) {
            Out.Failure(e)
        }
    }

    private fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            id = messageId,
            score = score,
            bubble = shortMessage,
            text = longMessage,
            isReceived = false,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun MessageEntity.toModel(): Message {
        return FirebaseMessage(
            messageId = id,
            score = score,
            shortMessage = bubble,
            longMessage = text,
        )
    }
}
