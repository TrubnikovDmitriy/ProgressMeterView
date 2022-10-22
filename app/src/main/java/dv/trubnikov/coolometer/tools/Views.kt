package dv.trubnikov.coolometer.tools

import android.view.LayoutInflater
import android.view.View

fun View.layoutInflater(): LayoutInflater {
    return LayoutInflater.from(context)
}