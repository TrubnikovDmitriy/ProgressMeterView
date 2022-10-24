package dv.trubnikov.coolometer.domain.parsers

import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.Out
import dv.trubnikov.coolometer.tools.failure
import dv.trubnikov.coolometer.tools.mapOut

class MessageParserImpl(
    private val parsers: Map<Class<*>, TypedMessageParser<*>>
) : MessageParser {

    override fun <T> internalParse(value: T, clazz: Class<T>): Out<Message> {
        return getParser(clazz).mapOut { it.parse(value) }
    }

    override fun <T> internalSerialize(message: Message, clazz: Class<T>): Out<T> {
        return getParser(clazz).mapOut { it.serialize(message) }
    }

    private fun <T> getParser(dataClazz: Class<T>): Out<TypedMessageParser<T>> {
        val parser = parsers[dataClazz]
        if (parser != null) {
            // We can be sure since it is guaranteed by DI
            @Suppress("UNCHECKED_CAST")
            return Out.Success(parser as TypedMessageParser<T>)
        } else {
            return failure { "There is no parser for data type [${dataClazz}]" }
        }
    }
}
