package dv.trubnikov.coolometer.domain.models

interface CloudMessage {
    val longMessage: String
    val shortMessage: String
    val score: Int
}

data class FirebaseMessage(
    override val longMessage: String,
    override val shortMessage: String,
    override val score: Int,
) : CloudMessage

class FakeMessage(
    override val longMessage: String = "Fake news!",
    override val shortMessage: String = "Fake +42",
    override val score: Int = +42,
) : CloudMessage
