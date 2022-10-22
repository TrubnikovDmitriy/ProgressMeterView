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
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE is_received=0")
    fun getUnreceivedMessages(): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET is_received='TRUE' WHERE id=(:messageId)")
    suspend fun markAsReceived(messageId: String)

    @Query("SELECT IFNULL(SUM(score), 0) FROM messages WHERE is_received=1")
    suspend fun getTotalScore(): Int
}
