package dv.trubnikov.coolometer.domain.models

import android.content.Intent
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

object CloudMessageParser {

    private const val EXPLAIN_KEY = "coolometer_explain_key"
    private const val BUBBLE_KEY = "coolometer_bubble_key"
    private const val SCORE_KEY = "coolometer_score_key"

    fun parse(message: RemoteMessage): CloudMessage? {
        val title = message.getOrLogError(EXPLAIN_KEY) ?: return null
        val text = message.getOrLogError(BUBBLE_KEY) ?: return null
        val scoreString = message.getOrLogError(SCORE_KEY) ?: return null
        val score = parseScoreOrLogError(scoreString) ?: return null
        return FirebaseMessage(
            longMessage = title,
            shortMessage = text,
            score = score
        )
    }

    fun parse(intent: Intent): CloudMessage? {
        val extras = intent.extras
        if (extras == null) {
            Timber.e("Отсутствуют extras у интента [$intent].")
            return null
        }
        val title = extras.getOrLogError(EXPLAIN_KEY) ?: return null
        val text = extras.getOrLogError(BUBBLE_KEY) ?: return null
        val scoreString = extras.getOrLogError(SCORE_KEY) ?: return null
        val score = parseScoreOrLogError(scoreString) ?: return null
        return FirebaseMessage(
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