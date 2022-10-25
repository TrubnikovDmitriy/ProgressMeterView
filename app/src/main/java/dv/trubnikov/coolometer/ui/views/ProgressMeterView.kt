package dv.trubnikov.coolometer.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.os.VibrationEffect
import android.os.VibrationEffect.EFFECT_CLICK
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.getVibratorManager
import dv.trubnikov.coolometer.ui.views.ProgressMeterDrawer.Companion.MAX_PROGRESS
import kotlin.math.abs
import kotlin.math.min

class ProgressMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    fun interface OvershootListener {
        fun onOvershoot(forward: Boolean)
    }

    private val drawer = ProgressMeterDrawer(context)
    private val vibrator = context.getVibratorManager()
    private val progressAnimator = ObjectAnimator.ofInt(
        this, "progress", 0
    )
    private val totalProgressAnimator = ObjectAnimator.ofInt(
        this, "totalProgress", 0
    )
    private val overshootAnimator = ValueAnimator.ofInt()
    private val bounceInterpolatorThreshold = 0.75f
    private val progressPerSecond = 0.35f

    var overshootListener: OvershootListener? = null

    var bigTickCount: Int
        get() = drawer.bigTickCount
        set(value) {
            drawer.bigTickCount = value
            invalidate()
        }

    var smallTickCount: Int
        get() = drawer.smallTickCount
        set(value) {
            drawer.smallTickCount = value
            invalidate()
        }

    var progress: Int
        get() = drawer.progress
        set(value) {
            drawer.progress = value
            invalidate()
        }

    var totalProgress: Int
        get() = drawer.totalProgress
        set(value) {
            drawer.totalProgress = value
            invalidate()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.ProgressMeterView, defStyleAttr, defStyleRes
        ).use {
            val rawBigTickCount = it.getInteger(R.styleable.ProgressMeterView_bigTicksCount, 5)
            val rawSmallTickCount = it.getInteger(R.styleable.ProgressMeterView_smallTicksCount, 2)
            val rawGravity = it.getInteger(R.styleable.ProgressMeterView_gravity, 0)

            drawer.bigTickCount = rawBigTickCount.coerceIn(3, 6)
            drawer.smallTickCount = rawSmallTickCount.coerceIn(0, 10)
            drawer.gravity = when(rawGravity) {
                0 -> ProgressMeterDrawer.Gravity.CENTER
                1 -> ProgressMeterDrawer.Gravity.TOP
                2 -> ProgressMeterDrawer.Gravity.BOTTOM
                else -> ProgressMeterDrawer.Gravity.CENTER
            }
        }
        setupVibrationForAfterAnimation()
        totalProgressAnimator.interpolator = LinearInterpolator()
    }

    /**
     * Add [value] to current [progress] and [totalProgress]
     *
     * @param value how much progress needs to be added
     * @param animate is need to animate the change
     *
     * @return true if the change was applied, false otherwise
     */
    fun addProgress(value: Int, animate: Boolean = false): Boolean {
        return addProgress(value, animate, force = false)
    }

    private fun addProgress(value: Int, animate: Boolean = false, force: Boolean = false): Boolean {
        if (progressAnimator.isRunning && !force) return false
        if (overshootAnimator.isRunning && !force) return false
        if (totalProgressAnimator.isRunning && !force) return false

        val newProgress = progress + value
        val normalizeProgress = newProgress.coerceIn(0, MAX_PROGRESS)

        // w/o animation
        if (!animate) {
            progress = newProgress % MAX_PROGRESS
            totalProgress += value
            return true
        }

        if (progress == 0 && value < 0) {
            backwardOvershootAnimation(value)
            return true
        }
        if (progress == MAX_PROGRESS && value >= 0) {
            forwardOvershootAnimation(value)
            return true
        }

        // with animation
        if (newProgress < 0) {
            val listener = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animator: Animator) {
                    progressAnimator.removeListener(this)
                    addProgress(newProgress, animate = true, force = true)
                }
            }
            progressAnimator.addListener(listener)
        }
        if (newProgress >= MAX_PROGRESS) {
            val listener = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animator: Animator) {
                    progressAnimator.removeListener(this)
                    addProgress(newProgress - MAX_PROGRESS, animate = true, force = true)
                }
            }
            progressAnimator.addListener(listener)
        }
        if (newProgress < 0 || MAX_PROGRESS < newProgress) {
            progressAnimator.interpolator = AccelerateInterpolator()
        } else {
            progressAnimator.interpolator = BounceInterpolator()
        }

        val timeSeconds = abs(normalizeProgress - progress).toFloat() / MAX_PROGRESS / progressPerSecond
        val timeMilliseconds = (1000 * timeSeconds).toLong()

        progressAnimator.setIntValues(progress, normalizeProgress)
        progressAnimator.duration = timeMilliseconds
        progressAnimator.start()

        totalProgressAnimator.setIntValues(totalProgress, totalProgress + normalizeProgress - progress)
        totalProgressAnimator.duration = timeMilliseconds
        totalProgressAnimator.start()

        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val desiredHeight = if (heightSpecMode == MeasureSpec.AT_MOST) {
            min(measuredHeight, heightSize)
        } else {
            measuredHeight
        }
        val desiredWidth = if (widthSpecMode == MeasureSpec.AT_MOST) {
            min(measuredWidth, widthSize)
        } else {
            measuredWidth
        }
        val resizeWidth = widthSpecMode != MeasureSpec.EXACTLY
        val resizeHeight = heightSpecMode != MeasureSpec.EXACTLY
        if (resizeWidth && desiredWidth > desiredHeight * drawer.widthHeightRatio) {
            setMeasuredDimension((desiredHeight * drawer.widthHeightRatio).toInt(), desiredHeight)
            return
        }
        if (resizeHeight && desiredHeight * drawer.widthHeightRatio > desiredWidth) {
            setMeasuredDimension(desiredWidth, (desiredWidth / drawer.widthHeightRatio).toInt())
            return
        }
        if (resizeHeight && resizeWidth) {
            val minWidth = minOf(desiredWidth.toFloat(), desiredHeight * drawer.widthHeightRatio)
            setMeasuredDimension(minWidth.toInt(), (minWidth / drawer.widthHeightRatio).toInt())
            return
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        drawer.setSize(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawer.draw(canvas, width, height)
    }

    private fun forwardOvershootAnimation(value: Int) {
        progress = 0
        overshootListener?.onOvershoot(forward = true)
        forwardNeedleRotation {
            addProgress(value, animate = true, force = true)
        }
    }

    private fun backwardOvershootAnimation(value: Int) {
        progress = MAX_PROGRESS
        overshootListener?.onOvershoot(forward = false)
        backwardNeedleRotation {
            addProgress(value, animate = true, force = true)
        }
    }

    private fun forwardNeedleRotation(onEndListener: () -> Unit) {
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                overshootAnimator.removeListener(this)
                progress = 0
                onEndListener()
            }
        }
        val outOfScale = 360 * MAX_PROGRESS / (180f - 2 * drawer.degreeOffset)
        overshootAnimator.addListener(listener)
        overshootAnimator.addUpdateListener {
            progress = it.animatedValue as Int
        }
        val passedProgress = (outOfScale - MAX_PROGRESS) / MAX_PROGRESS
        val speedForOvershooting = 10 * progressPerSecond
        val overshotDuration = (1000f * passedProgress / speedForOvershooting).toLong()
        val vibration = VibrationEffect.createOneShot(overshotDuration, 200)
        overshootAnimator.duration = overshotDuration
        overshootAnimator.setIntValues(MAX_PROGRESS, outOfScale.toInt())
        vibrator.defaultVibrator.vibrate(vibration)
        overshootAnimator.start()
    }

    private fun backwardNeedleRotation(onEndListener: () -> Unit) {
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                overshootAnimator.removeListener(this)
                progress = MAX_PROGRESS
                onEndListener()
            }
        }
        val outOfScale = 360 * MAX_PROGRESS / (180f - 2 * drawer.degreeOffset)
        overshootAnimator.addListener(listener)
        overshootAnimator.addUpdateListener {
            progress = it.animatedValue as Int
        }
        val passedProgress = (outOfScale - MAX_PROGRESS) / MAX_PROGRESS
        val speedForOvershooting = 10 * progressPerSecond
        val overshotDuration = (1000f * passedProgress / speedForOvershooting).toLong()
        overshootAnimator.duration = overshotDuration
        overshootAnimator.setIntValues(0, -outOfScale.toInt() + MAX_PROGRESS)
        overshootAnimator.start()
    }

    private fun setupVibrationForAfterAnimation() {
        val clickVibration = VibrationEffect.createPredefined(EFFECT_CLICK)
        var vibrateIsDone = false
        progressAnimator.interpolator = BounceInterpolator()
        progressAnimator.addUpdateListener {
            // These conditions only for [BounceInterpolator]!
            if (!vibrateIsDone
                && bounceInterpolatorThreshold <= it.animatedFraction
                && it.animatedFraction <= bounceInterpolatorThreshold + 0.05
            ) {
                vibrateIsDone = true
                vibrator.defaultVibrator.vibrate(clickVibration)
            }
            if (vibrateIsDone && it.animatedFraction > 0.95f) {
                vibrateIsDone = false
            }
        }
    }
}