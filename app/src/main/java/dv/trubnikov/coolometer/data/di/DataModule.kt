package dv.trubnikov.coolometer.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dv.trubnikov.coolometer.data.preferences.SharedPreferenceRepository
import dv.trubnikov.coolometer.data.room.AppDatabase
import dv.trubnikov.coolometer.data.room.dao.MessageDao
import dv.trubnikov.coolometer.data.room.repositories.RoomMessageRepository
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.domain.resositories.PreferenceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindMessageRepository(impl: RoomMessageRepository): MessageRepository

    @Binds
    @Singleton
    fun bindPreferenceRepository(impl: SharedPreferenceRepository): PreferenceRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "app-database"
            ).build()
        }

        @Provides
        @Singleton
        fun provideMessageDao(database: AppDatabase): MessageDao {
            return database.messageDao()
        }
    }
}