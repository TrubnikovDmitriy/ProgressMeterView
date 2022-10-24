package dv.trubnikov.coolometer.domain.parsers

import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.Out

interface TypedMessageParser<T> {

    fun parse(value: T): Out<Message>

    fun serialize(message: Message): Out<T>

    companion object {
        const val SCORE_KEY = "coolometer_score_key"
        const val TEXT_KEY = "coolometer_text_key"
        const val ID_KEY = "coolometer_id_key"
    }
}
