package dv.trubnikov.coolometer.domain.models

import java.util.*

interface Message {
    val messageId: String
    val text: String
    val score: Int
}

data class FirebaseMessage(
    override val messageId: String,
    override val text: String,
    override val score: Int,
) : Message

class FakeMessage(
    override val messageId: String = UUID.randomUUID().toString(),
    override val text: String = "Fake news!",
    override val score: Int = +42,
) : Message
