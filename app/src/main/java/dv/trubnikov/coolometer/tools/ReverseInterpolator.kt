package dv.trubnikov.coolometer.tools

import android.view.animation.Interpolator
import kotlin.math.abs

fun Interpolator.reverse(): Interpolator {
    return ReverseInterpolator(this)
}

private class ReverseInterpolator(
    private val interpolator: Interpolator
) : Interpolator {
    override fun getInterpolation(input: Float): Float {
        return abs(input - 1f)
    }
}
