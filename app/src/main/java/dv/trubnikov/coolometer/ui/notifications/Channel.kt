package dv.trubnikov.coolometer.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.getNotificationManager

enum class Channel(
    val id: String,
    private val importance: Int,
    @StringRes private val channelName: Int,
    @StringRes private val description: Int,
) {
    DEBUG(
        "debug_channel_id",
        NotificationManager.IMPORTANCE_DEFAULT,
        R.string.debug_notification_channel_name,
        R.string.debug_notification_channel_description
    );

    fun init(context: Context) {
        val name = context.getString(this.channelName)
        val channel = NotificationChannel(this.id, name, this.importance)
        channel.description = context.getString(this.description)
        val notificationManager = context.getNotificationManager()
        notificationManager.createNotificationChannel(channel)
    }
}
