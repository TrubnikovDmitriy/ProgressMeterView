package dv.trubnikov.coolometer.ui.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import dv.trubnikov.coolometer.tools.unsafeLazy
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt

class FloatingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val random by unsafeLazy { Random(System.currentTimeMillis()) }
    private val lift: ObjectAnimator
    private val swing: ObjectAnimator
    private val rotation: ObjectAnimator
    private val appearing: ObjectAnimator

    init {
        lift = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f).apply {
            interpolator = AccelerateInterpolator()
        }

        swing = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f).apply {
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            setCurrentFraction(random.nextFloat())
        }

        rotation = ObjectAnimator.ofFloat(this, View.ROTATION, -40f, +40f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            setCurrentFraction(random.nextFloat())
        }

        appearing = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)

        lift.doOnEnd { reset() }
        appearing.doOnStart { isVisible = true }
    }

    fun animateFloating(parentWidth: Float, parentHeight: Float) {
        reset()

        val baseDuration = 4_000L
        val totalDuration = (2 + random.nextFloat() * 3) * baseDuration // base * (2..5)
        val swingDuration = (1 + random.nextFloat() * 0.3) * baseDuration // base * (1,0..1,3)
        val appearDuration = baseDuration / 4f

        lift.apply {
            setFloatValues(0f, -parentHeight - max(width, height))
            interpolator = DecelerateInterpolator()
            duration = totalDuration.toLong()
        }

        swing.apply {
            duration = swingDuration.toLong()
            setCurrentFraction(random.nextInt(25..75) / 100f)
        }

        rotation.apply {
            duration = swingDuration.toLong()
            setCurrentFraction(random.nextFloat())
        }

        appearing.apply {
            duration = appearDuration.toLong()
        }

        val animator = object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                // We can't do it before since view is not inflated yet (width == 0)
                val alpha = animation.animatedValue as Float
                if (alpha <= 0f) return
                swing.setFloatValues(
                    -(parentWidth - width) / 2f,
                    +(parentWidth - width) / 2f,
                )
                appearing.removeUpdateListener(this)
            }
        }
        appearing.addUpdateListener(animator)

        lift.start()
        swing.start()
        rotation.start()
        appearing.start()
    }

    fun reset() {
        lift.cancel()
        swing.cancel()
        rotation.cancel()
        appearing.cancel()
        isVisible = false
        translationX = 0f
        translationY = 0f
        setRotation(0f)
    }
}