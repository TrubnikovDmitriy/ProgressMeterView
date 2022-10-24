package dv.trubnikov.coolometer.domain.parsers

import androidx.work.Data
import androidx.work.hasKeyWithValueOfType
import dv.trubnikov.coolometer.domain.models.FirebaseMessage
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.ID_KEY
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.SCORE_KEY
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.TEXT_KEY
import dv.trubnikov.coolometer.tools.Out
import dv.trubnikov.coolometer.tools.failure
import dv.trubnikov.coolometer.tools.getOr

object DataMessageParser : TypedMessageParser<Data> {

    override fun serialize(message: Message): Out<Data> {
        val data = Data.Builder()
            .putString(ID_KEY, message.messageId)
            .putString(TEXT_KEY, message.text)
            .putInt(SCORE_KEY, message.score)
            .build()

        return Out.Success(data)
    }

    override fun parse(value: Data): Out<Message> {
        val id = value.getStringOut(ID_KEY).getOr { return it }
        val score = value.getIntOut(SCORE_KEY).getOr { return it }
        val text = value.getStringOut(TEXT_KEY).getOr { return it }

        val message = FirebaseMessage(
            messageId = id,
            score = score,
            text = text,
        )
        return Out.Success(message)
    }

    private fun Data.getStringOut(key: String): Out<String> {
        if (hasKeyWithValueOfType<String>(key)) {
            val value = requireNotNull(getString(key))
            return Out.Success(value)
        } else {
            return failure {
                "Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this]."
            }
        }
    }

    private fun Data.getIntOut(key: String): Out<Int> {
        if (hasKeyWithValueOfType<Int>(key)) {
            val value = getInt(key, 0)
            return Out.Success(value)
        } else {
            return failure {
                "Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this]."
            }
        }
    }
}
