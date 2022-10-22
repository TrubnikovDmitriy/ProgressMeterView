package dv.trubnikov.coolometer.ui.debug

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dv.trubnikov.coolometer.R

sealed class DebugItem(val viewType: Int) {

    data class Button(
        @StringRes val text: Int,
        @DrawableRes val icon: Int,
        val listener: Listener,
    ) : DebugItem(viewType = 0) {
        fun interface Listener {
            fun onClick()
        }
    }

    data class Switch(
        @StringRes val text: Int,
        val listener: Listener,
    ) : DebugItem(viewType = 1) {
        fun interface Listener {
            fun onSwitch(isChecked: Boolean)
        }
    }

    data class Spinner(
        @StringRes val text: Int,
        @ArrayRes val array: Int,
        val listener: Listener,
    ) : DebugItem(viewType = 2) {
        fun interface Listener {
            fun onItemClick(position: Int)
        }
    }
}
