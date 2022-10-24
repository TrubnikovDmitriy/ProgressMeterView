package dv.trubnikov.coolometer.data.room.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "is_received")
    val isReceived: Boolean,

    @ColumnInfo(name = "time")
    val timestamp: Long,
)