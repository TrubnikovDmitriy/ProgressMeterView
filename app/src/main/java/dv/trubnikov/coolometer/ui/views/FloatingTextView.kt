package dv.trubnikov.coolometer.ui.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import kotlin.random.Random

class FloatingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val random = Random(System.currentTimeMillis())

    fun animateFloating(parentWidth: Float, parentHeight: Float) {
        val baseDuration = 4_000L
        val totalDuration = (2 + random.nextFloat() * 3) * baseDuration // base * (3..6)
        val swingDuration = (1 + random.nextFloat() * 0.3) * baseDuration // base * (1,0..1,3)
        val appearDuration = baseDuration / 2f

        val lift = ObjectAnimator.ofFloat(
            this, View.TRANSLATION_Y,
            0f,
            -parentHeight
        ).apply {
            interpolator = DecelerateInterpolator()
            duration = totalDuration.toLong()
        }

        val swing = ObjectAnimator.ofFloat(
            this, View.TRANSLATION_X,
            -(parentWidth - width) / 2f,
            +(parentWidth - width) / 2f,
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = swingDuration.toLong()
            setCurrentFraction(0.5f)
        }

        val rotation = ObjectAnimator.ofFloat(
            this, View.ROTATION,
            -40f,
            +40f,
        ).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = swingDuration.toLong()
            setCurrentFraction(0.5f)
        }

        val appearing = ObjectAnimator.ofFloat(
            this, View.ALPHA,
            0f,
            1f,
        ).apply {
            duration = appearDuration.toLong()
        }

        lift.start()
        swing.start()
        rotation.start()
        appearing.start()

        lift.doOnEnd {
            swing.cancel()
            rotation.cancel()
            isVisible = false
            translationX = 0f
            translationY = 0f
            setRotation(0f)
        }
        appearing.doOnStart {
            isVisible = true
        }
    }
}