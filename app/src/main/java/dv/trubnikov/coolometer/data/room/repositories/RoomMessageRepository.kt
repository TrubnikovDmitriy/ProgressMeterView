package dv.trubnikov.coolometer.data.room.repositories

import android.database.sqlite.SQLiteException
import dv.trubnikov.coolometer.data.room.dao.MessageDao
import dv.trubnikov.coolometer.data.room.tables.MessageEntity
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.MessageParser
import dv.trubnikov.coolometer.domain.parsers.MessageParser.Companion.parse
import dv.trubnikov.coolometer.domain.parsers.MessageParser.Companion.serialize
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.Out
import dv.trubnikov.coolometer.tools.getOr
import dv.trubnikov.coolometer.tools.getOrThrow
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
    private val parser: MessageParser,
) : MessageRepository {

    override suspend fun insertMessage(message: Message): Out<Unit> {
        val entity = parser.serialize<MessageEntity>(message).getOr { return it }
        return safeDatabaseRequest(Dispatchers.IO) {
            messageDao.insertMessage(entity)
        }
    }

    override suspend fun getUnreceivedMessages(): Flow<List<Message>> {
        return flow {
            messageDao.getUnreceivedMessages().collect { messages ->
                val models = messages.map { parser.parse(it).getOrThrow() }
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
}
