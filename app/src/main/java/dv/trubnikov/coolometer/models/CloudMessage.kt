package dv.trubnikov.coolometer.models

import android.provider.MediaStore.Audio.AudioColumns.TITLE_KEY
import com.google.firebase.messaging.RemoteMessage

data class CloudMessage(
    val title: String,
    val text: String,
    val score: Int,
)
