package dv.trubnikov.coolometer.domain.parsers

import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.Out

/**
 * Universal parser with convenient API
 * (actually, it is just a composition of [TypedMessageParser])
 */
interface MessageParser {
    companion object {
        inline fun <reified T> MessageParser.parse(value: T): Out<Message> {
            return internalParse(value, T::class.java)
        }

        inline fun <reified T> MessageParser.serialize(message: Message): Out<T> {
            return internalSerialize(message, T::class.java)
        }
    }

    fun <T> internalParse(value: T, clazz: Class<T>): Out<Message>

    fun <T> internalSerialize(message: Message, clazz: Class<T>): Out<T>
}