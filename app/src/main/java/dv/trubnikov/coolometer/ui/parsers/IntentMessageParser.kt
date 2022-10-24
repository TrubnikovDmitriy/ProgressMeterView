package dv.trubnikov.coolometer.ui.parsers

import android.content.Intent
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

object IntentMessageParser : TypedMessageParser<Intent> {

    override fun parse(value: Intent): Out<Message> {
        val extras = value.extras ?: return failure {
            "Отсутствуют extras у интента [$value]."
        }

        val id = extras.getOrLogError(ID_KEY).getOr { return it }
        val title = extras.getOrLogError(TEXT_KEY).getOr { return it }
        val scoreString = extras.getOrLogError(SCORE_KEY).getOr { return it }
        val score = parseScoreOrLogError(scoreString).getOr { return it }

        val message = FirebaseMessage(
            messageId = id,
            text = title,
            score = score
        )

        return Out.Success(message)
    }

    override fun serialize(message: Message): Out<Intent> {
        return failure { "This method is not implemented" }
    }

    private fun parseScoreOrLogError(score: String): Out<Int> {
        return try {
            Out.Success(score.toInt())
        } catch (e: NumberFormatException) {
            return Out.Failure(e, "Score не является числом score=[$score].")
        }
    }

    private fun Bundle.getOrLogError(key: String): Out<String> {
        val value = getString(key)
        if (value != null) {
            return Out.Success(value)
        } else {
            return failure { "Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this]." }
        }
    }
}
