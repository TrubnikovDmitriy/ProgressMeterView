package dv.trubnikov.coolometer.app.di

import android.content.Intent
import androidx.work.Data
import com.google.firebase.messaging.RemoteMessage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import dv.trubnikov.coolometer.data.parsers.EntityMessageParser
import dv.trubnikov.coolometer.data.parsers.RemoteMessageParser
import dv.trubnikov.coolometer.data.room.tables.MessageEntity
import dv.trubnikov.coolometer.domain.parsers.DataMessageParser
import dv.trubnikov.coolometer.domain.parsers.MessageParser
import dv.trubnikov.coolometer.domain.parsers.MessageParserImpl
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser
import dv.trubnikov.coolometer.ui.parsers.IntentMessageParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideMessageParser(
        parsers: Map<Class<*>, @JvmSuppressWildcards TypedMessageParser<*>>,
    ): MessageParser {
        return MessageParserImpl(parsers)
    }

    @IntoMap
    @Provides
    @ClassKey(Data::class)
    fun dataParser(): TypedMessageParser<*> = DataMessageParser

    @IntoMap
    @Provides
    @ClassKey(Intent::class)
    fun intentMessageParser(): TypedMessageParser<*> = IntentMessageParser

    @IntoMap
    @Provides
    @ClassKey(RemoteMessage::class)
    fun remoteMessageParser(): TypedMessageParser<*> = RemoteMessageParser

    @IntoMap
    @Provides
    @ClassKey(MessageEntity::class)
    fun entityMessageParser(): TypedMessageParser<*> = EntityMessageParser
}