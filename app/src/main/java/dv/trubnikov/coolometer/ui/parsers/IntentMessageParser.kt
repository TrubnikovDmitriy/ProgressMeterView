package dv.trubnikov.coolometer.ui.parsers

import android.content.Intent
import android.net.ParseException
import android.os.Bundle
import dv.trubnikov.coolometer.domain.models.FirebaseMessage
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.ID_KEY
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.SCORE_KEY
import dv.trubnikov.coolometer.domain.parsers.TypedMessageParser.Companion.TEXT_KEY
import dv.trubnikov.coolometer.tools.Out
import dv.trubnikov.coolometer.tools.failure
import dv.trubnikov.coolometer.tools.getOr
import dv.trubnikov.coolometer.tools.toSignString

object IntentMessageParser : TypedMessageParser<Intent> {

    override fun parse(value: Intent): Out<Message> {
        val extras = value.extras ?: return failure {
            "Отсутствуют extras у интента [$value]."
        }

        val id = extras.getOrLogError(ID_KEY).getOr { return it }
        val title = extras.getOrLogError(TEXT_KEY).getOr { return it }
        val score = extras.parseScore().getOr { return it }

        val message = FirebaseMessage(
            messageId = id,
            text = title,
            score = score
        )

        return Out.Success(message)
    }

    override fun serialize(message: Message): Out<Intent> {
        val intent = Intent().apply {
            putExtra(ID_KEY, message.messageId)
            putExtra(TEXT_KEY, message.text)
            putExtra(SCORE_KEY, message.score.toSignString())
        }
        return Out.Success(intent)
    }

    private fun Bundle.getOrLogError(key: String): Out<String> {
        val value = getString(key)
        if (value != null) {
            return Out.Success(value)
        } else {
            return Out.Failure(
                ParseException("Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this].")
            )
        }
    }

    private fun Bundle.parseScore(): Out<Int> {
        if (!containsKey(SCORE_KEY)) {
            val exception = ParseException(
                "Отсутствует ключ [$SCORE_KEY]. Не удалось распарсить сообщение [$this]."
            )
            return Out.Failure(exception)
        }
        val score = getOrLogError(SCORE_KEY)
        return if (score is Out.Success) {
            try {
                Out.Success(score.value.toInt())
            } catch (e: NumberFormatException) {
                Out.Failure(e, "Score не является числом score=[${score.value}].")
            }
        } else {
            val scoreInt = getInt(SCORE_KEY)
            Out.Success(scoreInt)
        }
    }
}
