package dv.trubnikov.coolometer.domain.models

import android.content.Intent
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

// TODO: It is shouldn't be in domain
object CloudMessageParser {

    private const val BUBBLE_KEY = "coolometer_bubble_key"
    private const val SCORE_KEY = "coolometer_score_key"
    private const val TEXT_KEY = "coolometer_text_key"
    private const val ID_KEY = "coolometer_id_key"

    fun parse(message: RemoteMessage): Message? {
        val id = message.getOrLogError(ID_KEY) ?: return null
        val title = message.getOrLogError(TEXT_KEY) ?: return null
        val text = message.getOrLogError(BUBBLE_KEY) ?: return null
        val scoreString = message.getOrLogError(SCORE_KEY) ?: return null
        val score = parseScoreOrLogError(scoreString) ?: return null
        return FirebaseMessage(
            messageId = id,
            longMessage = title,
            shortMessage = text,
            score = score
        )
    }

    fun parse(intent: Intent): Message? {
        val extras = intent.extras
        if (extras == null) {
            Timber.e("Отсутствуют extras у интента [$intent].")
            return null
        }
        val id = extras.getOrLogError(ID_KEY) ?: return null
        val title = extras.getOrLogError(TEXT_KEY) ?: return null
        val text = extras.getOrLogError(BUBBLE_KEY) ?: return null
        val scoreString = extras.getOrLogError(SCORE_KEY) ?: return null
        val score = parseScoreOrLogError(scoreString) ?: return null
        return FirebaseMessage(
            messageId = id,
            longMessage = title,
            shortMessage = text,
            score = score
        )
    }

    private fun parseScoreOrLogError(score: String): Int? {
        return try {
            score.toInt()
        } catch (e: NumberFormatException) {
            Timber.e(e,"Score не является числом score=[$score].")
            return null
        }
    }

    private fun RemoteMessage.getOrLogError(key: String): String? {
        return data.getOrElse(key) {
            Timber.e("Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this].")
            null
        }
    }

    private fun Bundle.getOrLogError(key: String): String? {
        val value = getString(key)
        if (value == null) {
            Timber.e("Отсутствует ключ [$key]. Не удалось распарсить сообщение [$this].")
            return null
        } else {
            return value
        }
    }
}