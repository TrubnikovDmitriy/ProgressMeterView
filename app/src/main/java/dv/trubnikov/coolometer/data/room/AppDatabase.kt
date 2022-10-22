package dv.trubnikov.coolometer.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import dv.trubnikov.coolometer.data.room.dao.MessageDao
import dv.trubnikov.coolometer.data.room.tables.MessageEntity

@Database(entities = [MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
