package dv.trubnikov.coolometer.models

import android.content.Intent
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

object CloudMessageParser {

    private const val TITLE_KEY = "cool_title"
    private const val TEXT_KEY = "cool_text"
    private const val SCORE_KEY = "cool_score"

    fun parse(message: RemoteMessage): CloudMessage? {
        val title = message.getOrLogError(TITLE_KEY) ?: return null
        val text = message.getOrLogError(TEXT_KEY) ?: return null
        val scoreString = message.getOrLogError(SCORE_KEY) ?: return null
        val score = parseScoreOrLogError(scoreString) ?: return null
        return CloudMessage(
            title = title,
            text = text,
            score = score
        )
    }

    fun parse(intent: Intent): CloudMessage? {
        val extras = intent.extras
        if (extras == null) {
            Timber.e("Отсутствуют extras у интента [$intent].")
            return null
        }
        val title = extras.getOrLogError(TITLE_KEY) ?: return null
        val text = extras.getOrLogError(TEXT_KEY) ?: return null
        val scoreString = extras.getOrLogError(SCORE_KEY) ?: return null
        val score = parseScoreOrLogError(scoreString) ?: return null
        return CloudMessage(
            title = title,
            text = text,
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