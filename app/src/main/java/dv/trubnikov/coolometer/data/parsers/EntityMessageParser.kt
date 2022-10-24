package dv.trubnikov.coolometer.data.parsers

import dv.trubnikov.coolometer.data.room.tables.MessageEntity
import dv.trubnikov.coolometer.domain.models.FirebaseMessage
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser
import dv.trubnikov.coolometer.tools.Out

object EntityMessageParser : TypedMessageParser<MessageEntity> {

    override fun parse(value: MessageEntity): Out<Message> {
        val message = FirebaseMessage(
            messageId = value.id,
            score = value.score,
            text = value.text,
        )
        return Out.Success(message)
    }

    override fun serialize(message: Message): Out<MessageEntity> {
        val entity = MessageEntity(
            id = message.messageId,
            score = message.score,
            text = message.text,
            isReceived = false,
            timestamp = System.currentTimeMillis()
        )
        return Out.Success(entity)
    }
}
