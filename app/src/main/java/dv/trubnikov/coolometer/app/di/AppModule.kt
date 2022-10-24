package dv.trubnikov.coolometer.app.di

import android.content.Context
import androidx.work.Data
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dv.trubnikov.coolometer.data.room.repositories.RoomMessageRepository
import dv.trubnikov.coolometer.domain.parsers.DataMessageParser
import dv.trubnikov.coolometer.domain.parsers.MessageParser
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            return crashlytics
        }

        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging {
            return FirebaseMessaging.getInstance()
        }

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext appContext: Context): WorkManager {
            return WorkManager.getInstance(appContext)
        }
    }

    @Binds
    @Singleton
    fun bindMessageRepository(impl: RoomMessageRepository): MessageRepository
}