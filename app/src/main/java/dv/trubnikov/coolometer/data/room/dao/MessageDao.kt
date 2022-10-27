package dv.trubnikov.coolometer.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dv.trubnikov.coolometer.data.room.tables.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessageBlocking(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE is_received=1 ORDER BY time")
    fun getReceivedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE is_received=0 ORDER BY time")
    fun getUnreceivedMessages(): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET is_received='1', time=(:timestamp)  WHERE id=(:messageId)")
    suspend fun markAsReceived(messageId: String, timestamp: Long)

    @Query("DELETE FROM messages WHERE id=(:messageId)")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT IFNULL(SUM(score), 0) FROM messages WHERE is_received=1")
    suspend fun getTotalScore(): Int
}
