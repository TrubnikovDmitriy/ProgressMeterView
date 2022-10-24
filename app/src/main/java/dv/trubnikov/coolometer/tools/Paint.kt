package dv.trubnikov.coolometer.tools

import android.graphics.Paint
import android.graphics.Rect
import java.lang.Float.min

private val bounds = Rect()

/**
 * Sets the text size for a Paint object so a given string of text will be a
 * given width.
 *
 * @param paint the Paint to set the text size for
 * @param desiredWidth the desired width
 * @param text the text that should be that width
 *
 * @see <a href="https://stackoverflow.com/a/21895626/8173261">https://stackoverflow.com/a/21895626/8173261</a>
 */
fun Paint.setTextSizeForSize(
    desiredWidth: Float,
    desiredHeight: Float,
    text: String,
) {
    val testTextSize = 48f

    // Get the bounds of the text, using our testTextSize.
    textSize = testTextSize
    getTextBounds(text, 0, text.length, bounds)

    // Calculate the desired size as a proportion of our testTextSize.
    val desiredTextSizeByHeight = testTextSize * desiredHeight / bounds.height()
    val desiredTextSizeByWidth = testTextSize * desiredWidth / bounds.width()

    // Set the paint for that size.
    textSize = min(desiredTextSizeByWidth, desiredTextSizeByHeight)
}
