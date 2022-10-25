package dv.trubnikov.coolometer.ui.screens.main

import android.app.Activity
import android.content.Context
import android.text.SpannableStringBuilder
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.toStringWithSign
import kotlin.math.absoluteValue

fun Activity.showNewMessageDialog(message: Message, onAccept: (Message) -> Unit) {
    val title = buildTitle(R.string.alert_dialog_new_message, message.score)
    AlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message.text)
        .setPositiveButton(R.string.generic_accept) { _, _ -> onAccept(message) }
        .setOnDismissListener { onAccept(message) }
        .show()
}

fun Activity.showAcceptMessage(message: Message, onAccept: (Message) -> Unit) {
    val title = buildTitle(R.string.alert_dialog_accept_message, message.score)
    AlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message.text)
        .setPositiveButton(R.string.generic_accept) { _, _ -> onAccept(message) }
        .setOnDismissListener { onAccept(message) }
        .show()
}

fun Activity.showPityDialog() {
    AlertDialogBuilder(this)
        .setTitle(R.string.alert_dialog_already_received_title)
        .setMessage(R.string.alert_dialog_already_received_text)
        .setPositiveButton(R.string.generic_okay, null)
        .show()
}

fun Activity.showChoiceDialog(messages: List<Message>, onClickItem: (Message) -> Unit) {
    if (messages.isEmpty()) return
    val maxMessageLength = 50
    val items = messages.map {
        val cutMessage = it.text.take(maxMessageLength)
        if (cutMessage.length == maxMessageLength) {
            "$cutMessage…"
        } else {
            cutMessage
        }
    }.toTypedArray()
    var checkedIndex = 0
    AlertDialogBuilder(this)
        .setSingleChoiceItems(items, checkedIndex) { _, index -> checkedIndex = index }
        .setTitle(R.string.alert_dialog_choice_title)
        .setPositiveButton(R.string.generic_accept) { _, _ -> onClickItem(messages[checkedIndex]) }
        .setNegativeButton(R.string.generic_close, null)
        .show()
}

private fun Activity.buildTitle(@StringRes textId: Int, score: Int): SpannableStringBuilder {
    val absScore = score.absoluteValue
    val textScore = score.toStringWithSign()
    val titleScore = resources.getQuantityString(R.plurals.alert_dialog_received_score, absScore, textScore)
    return SpannableStringBuilder()
        .append(getString(textId))
        .append(" ($titleScore)")
}

@Suppress("FunctionName")
private fun AlertDialogBuilder(context: Context): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(context, R.style.Cool_MaterialAlertDialog)
}
