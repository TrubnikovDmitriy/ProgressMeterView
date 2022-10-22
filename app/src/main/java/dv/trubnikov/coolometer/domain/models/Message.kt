package dv.trubnikov.coolometer.domain.models

import java.util.*

interface Message {
    val messageId: String
    val longMessage: String
    val shortMessage: String
    val score: Int
}

data class FirebaseMessage(
    override val messageId: String,
    override val longMessage: String,
    override val shortMessage: String,
    override val score: Int,
) : Message

class FakeMessage(
    override val messageId: String = UUID.randomUUID().toString(),
    override val longMessage: String = "Fake news!",
    override val shortMessage: String = "Fake +42",
    override val score: Int = +42,
) : Message
