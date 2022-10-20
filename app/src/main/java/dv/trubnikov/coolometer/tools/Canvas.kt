package dv.trubnikov.coolometer.tools

import android.graphics.Canvas

/**
 * Transforms android views' coordinate system into the usual mathematical one.
 */
fun Canvas.withMathCoordinates(width: Int, height: Int, block: Canvas.() -> Unit) {
    val checkpoint = save()
    translate(width / 2f, -height / 2f)
    scale(1f, -1f, width / 2f, height / 2f)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}