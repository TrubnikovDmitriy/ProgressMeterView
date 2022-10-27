package dv.trubnikov.coolometer.ui.screens.history

import androidx.annotation.ColorRes
import java.util.*

data class HistoryItem(
    val score: Int,
    val text: String,
    val date: Date,
    @ColorRes val color: Int,
)