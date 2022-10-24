package dv.trubnikov.coolometer.data.parsers

import com.google.firebase.messaging.RemoteMessage
import dv.trubnikov.coolometer.domain.models.FirebaseMessage
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.ID_KEY
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.SCORE_KEY
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.TEXT_KEY
import dv.trubnikov.coolometer.tools.Out
import dv.trubnikov.coolometer.tools.failure
import dv.trubnikov.coolometer.tools.getOr

object RemoteMessageParser : TypedMessageParser<RemoteMessage> {

    override fun parse(value: RemoteMessage): Out<Message> {
        val id = value.getOrLogError(ID_KEY).getOr { return it }
        val title = value.getOrLogError(TEXT_KEY).getOr { return it }
        val scoreString = value.getOrLogError(SCORE_KEY).getOr { return it }
        val score = parseScoreOrLogError(scoreString).getOr { return it }

        val message = FirebaseMessage(
            messageId = id,
            text = title,
            score = score
        )

        return Out.Success(message)
    }

    override fun serialize(message: Message): Out<RemoteMessage> {
        return failure { "This method is not implemented" }
    }

    private fun parseScoreOrLogError(score: String): Out<Int> {
        return try {
            Out.Success(score.toInt())
        } catch (e: NumberFormatException) {
            return Out.Failure(e, "Score не является числом score=[$score].")
        }
    }

    private fun RemoteMessage.getOrLogError(key: String): Out<String> {
        val value = data[key]
        if (value != null) {
            return Out.Success(value)
        } else {
            return failure { "Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this]." }
        }
    }
}
