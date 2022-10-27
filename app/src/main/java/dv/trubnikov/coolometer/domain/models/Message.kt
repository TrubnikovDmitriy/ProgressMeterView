package dv.trubnikov.coolometer.domain.models

import java.util.*

interface Message {
    val messageId: String
    val text: String
    val score: Int
    val timestamp: Long
}

data class FirebaseMessage(
    override val messageId: String,
    override val text: String,
    override val score: Int,
    override val timestamp: Long = System.currentTimeMillis(),
) : Message

class FakeMessage(
    override val messageId: String = UUID.randomUUID().toString(),
    override val text: String = "Fake news!",
    override val score: Int = +42,
    override val timestamp: Long = System.currentTimeMillis(),
) : Message
