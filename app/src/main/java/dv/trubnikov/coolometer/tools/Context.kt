package dv.trubnikov.coolometer.tools

import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ClipboardManager
import android.content.Context
import android.os.VibratorManager
import androidx.core.content.getSystemService

fun Context.getVibratorManager() = getSystemService<VibratorManager>()!!
fun Context.getClipboardManager() = getSystemService<ClipboardManager>()!!
fun Context.getAppWidgetManager() = getSystemService<AppWidgetManager>()!!
fun Context.getNotificationManager() = getSystemService<NotificationManager>()!!
