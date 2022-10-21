package dv.trubnikov.coolometer.tools

import android.content.Context
import android.os.VibratorManager
import androidx.core.content.getSystemService

fun Context.getVibratorManager() = getSystemService<VibratorManager>()!!
